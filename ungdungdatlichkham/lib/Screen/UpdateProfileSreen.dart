import 'dart:io';
import 'package:flutter/material.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:image_picker/image_picker.dart';
import 'package:intl/intl.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/User.dart';

class UpdateProfileScreen extends StatefulWidget {
  const UpdateProfileScreen({Key? key}) : super(key: key);

  @override
  _UpdateProfileScreenState createState() => _UpdateProfileScreenState();
}

class _UpdateProfileScreenState extends State<UpdateProfileScreen> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _phoneController = TextEditingController();
  final TextEditingController _addressController = TextEditingController();
  final TextEditingController _dateController = TextEditingController();
  final TextEditingController _emailController= TextEditingController();
  String _selectedGender = "Nam";
  File? _image;
  String? _avatarUrl;
  User? _currentUser;

  final DatabaseReference _databaseRef = FirebaseDatabase.instance.ref().child('user');
  final FirebaseStorage _storage = FirebaseStorage.instance;

  @override
  void initState() {
    super.initState();
    _loadUserData();
  }

  Future<void> _loadUserData() async {
    final prefs = await SharedPreferences.getInstance();
    final userId = prefs.getString('userId');

    if (userId != null) {
      final snapshot = await _databaseRef.child(userId).get();
      if (snapshot.exists) {
        final userData = Map<String, dynamic>.from(snapshot.value as Map);
        setState(() {
          _currentUser = User.fromMap(userData);

          // Gán thông tin vào các `TextField`
          _nameController.text = _currentUser?.name ?? '';
          _phoneController.text = _currentUser?.phone ?? '';
          _addressController.text = _currentUser?.address ?? '';
          _emailController.text= _currentUser?.email??'';
          _selectedGender = (_currentUser?.sex ?? true) ? "Nam" : "Nữ";

          if (_currentUser?.dateOfBirth != null) {
            final dateOfBirth = DateTime.parse(_currentUser!.dateOfBirth!);
            _dateController.text = DateFormat('dd/MM/yyyy').format(dateOfBirth);
          }

          // Gán avatar URL nếu có
          if (_currentUser?.avatar != null && _currentUser!.avatar!.isNotEmpty) {
            _avatarUrl = _currentUser!.avatar!;
          }
        });
      }
    }
  }

  Future<void> _pickImage() async {
    final picker = ImagePicker();
    final pickedImage = await picker.pickImage(source: ImageSource.gallery);
    if (pickedImage != null) {
      setState(() {
        _image = File(pickedImage.path);
      });
    }
  }

  Future<void> _updateProfile() async {
    if (!_formKey.currentState!.validate()) return;

    try {
      final updatedData = <String, dynamic>{};

      if (_nameController.text.trim() != _currentUser?.name) {
        updatedData['name'] = _nameController.text.trim();
      }
      if (_phoneController.text.trim() != _currentUser?.phone) {
        updatedData['phone'] = _phoneController.text.trim();
      }
      if (_addressController.text.trim() != _currentUser?.address) {
        updatedData['address'] = _addressController.text.trim();
      }
      if (_emailController.text.trim() != _currentUser?.email) {
        updatedData['email'] = _emailController.text.trim();
      }
      if (_dateController.text.isNotEmpty &&
          DateFormat('dd/MM/yyyy').parse(_dateController.text.trim()).toString() != _currentUser?.dateOfBirth) {
        updatedData['date_of_birth'] = DateFormat('yyyy-MM-dd').format(
          DateFormat('dd/MM/yyyy').parse(_dateController.text.trim()),
        );
      }
      if ((_selectedGender == "Nam" && _currentUser?.sex != true) ||
          (_selectedGender == "Nữ" && _currentUser?.sex != false)) {
        updatedData['sex'] = _selectedGender == "Nam" ? true : false;
      }

      if (_image != null) {
        final storageRef = _storage.ref().child('users/${_currentUser?.userId}/avatar.jpg');
        final uploadTask = await storageRef.putFile(_image!);
        final newAvatarUrl = await uploadTask.ref.getDownloadURL();
        updatedData['avatar'] = newAvatarUrl;
      }

      if (updatedData.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Không có thông tin nào thay đổi')),
        );
        return;
      }

      await _databaseRef.child(_currentUser!.userId!).update(updatedData);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Cập nhật thông tin thành công!')),
      );

      setState(() {
        _currentUser = User.fromMap({..._currentUser!.toMap(), ...updatedData});
      });
      // Trở về màn hình trước đó
      Navigator.pop(context, true); // Truyền giá trị `true` để báo hiệu cập nhật thành công
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Lỗi: $e')),
      );
    }
  }

  Future<void> _showCenteredDatePicker(BuildContext context) async {
    DateTime initialDate = _dateController.text.isNotEmpty
        ? DateFormat('dd/MM/yyyy').parse(_dateController.text)
        : DateTime.now();
    DateTime? pickedDate;

    await showDialog(
      context: context,
      builder: (BuildContext context) {
        return Dialog(
          backgroundColor: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16.0),
          ),
          child: Container(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Text(
                  "Chọn ngày sinh",
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 16),
                CalendarDatePicker(
                  initialDate: initialDate,
                  firstDate: DateTime(1900),
                  lastDate: DateTime.now(),
                  onDateChanged: (DateTime date) {
                    pickedDate = date;
                  },
                ),
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    TextButton(
                      onPressed: () {
                        Navigator.pop(context);
                      },
                      child: const Text("Hủy", style: TextStyle(fontSize: 17, color: Colors.red)),
                    ),
                    ElevatedButton(
                      onPressed: () {
                        if (pickedDate != null) {
                          setState(() {
                            _dateController.text = DateFormat('dd/MM/yyyy').format(pickedDate!);
                          });
                        }
                        Navigator.pop(context);
                      },
                      child: const Text("Xác nhận", style: TextStyle(fontSize: 17, color: Colors.white)),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Cập nhật thông tin cá nhân"
        , style:  TextStyle(
            fontSize: 23,
            fontWeight: FontWeight.w500,
            fontFamily: 'Rotobo'
          ),),
        leading: IconButton(
          icon: Icon(Icons.arrow_back_ios_new,
          color:Color.fromARGB(255, 47, 100, 253),
          size: 25,),
          onPressed: (){
            Navigator.pop(context);
          },
        ),
        backgroundColor: Colors.white,
      ),
      backgroundColor: Colors.white,
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              GestureDetector(
                onTap: _pickImage,
                child: CircleAvatar(
                  radius: 75,
                  backgroundImage: _image != null
                      ? FileImage(_image!)
                      : (_avatarUrl != null && _avatarUrl!.isNotEmpty
                      ? NetworkImage(_avatarUrl!)
                      : null) as ImageProvider?,
                  child: _image == null && (_avatarUrl == null || _avatarUrl!.isEmpty)
                      ? const Icon(Icons.camera_alt, size: 50)
                      : null,
                ),
              ),
              const SizedBox(height: 20),
              TextFormField(
                controller: _nameController,
                decoration: InputDecoration(
                  labelText: 'Họ tên',
                  labelStyle: TextStyle(color: Colors.grey.shade600, fontSize: 17),
                  floatingLabelStyle: TextStyle(color: Color.fromARGB(255, 47, 100, 253), fontSize: 18),
                  enabledBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Colors.grey.shade300),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Color.fromARGB(255, 47, 100, 253)),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  errorBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Colors.red),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  filled: true,
                  fillColor: Colors.grey.shade100,
                  prefixIcon: Icon(Icons.person_outline, color: Color.fromARGB(255, 47, 100, 253)),
                ),
                style: TextStyle(fontSize: 20),
                validator: (value) => value!.isEmpty ? 'Vui lòng nhập họ tên' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _phoneController,
                keyboardType: TextInputType.phone,
                decoration: InputDecoration(
                  labelText: 'Số điện thoại',
                  labelStyle: TextStyle(color: Colors.grey.shade600, fontSize: 17),
                  floatingLabelStyle: TextStyle(color: Color.fromARGB(255, 47, 100, 253), fontSize: 18),
                  enabledBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Colors.grey.shade300),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Color.fromARGB(255, 47, 100, 253)),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  errorBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Colors.red),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  filled: true,
                  fillColor: Colors.grey.shade100,
                  prefixIcon: Icon(Icons.phone_outlined, color: Color.fromARGB(255, 47, 100, 253)),
                ),
                style: TextStyle(fontSize: 20),
                validator: (value) => value!.isEmpty ? 'Vui lòng nhập số điện thoại' : null,
              ),
              const SizedBox(height: 16),
              GestureDetector(
                onTap: () => _showCenteredDatePicker(context),
                child: AbsorbPointer(
                  child: TextFormField(
                    controller: _dateController,
                    decoration: InputDecoration(
                      labelText: 'Ngày sinh',
                      labelStyle: TextStyle(color: Colors.grey.shade600, fontSize: 17),
                      floatingLabelStyle: TextStyle(color: Color.fromARGB(255, 47, 100, 253), fontSize: 18),
                      enabledBorder: OutlineInputBorder(
                        borderSide: BorderSide(color: Colors.grey.shade300),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      focusedBorder: OutlineInputBorder(
                        borderSide: BorderSide(color: Color.fromARGB(255, 47, 100, 253)),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      errorBorder: OutlineInputBorder(
                        borderSide: BorderSide(color: Colors.red),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      filled: true,
                      fillColor: Colors.grey.shade100,
                      prefixIcon: Icon(Icons.cake_outlined, color: Color.fromARGB(255, 47, 100, 253)),
                    ),
                    style: TextStyle(fontSize: 20),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                value: _selectedGender,
                items: const [
                  DropdownMenuItem(value: "Nam", child: Text("Nam", style: TextStyle(color: Colors.black))),
                  DropdownMenuItem(value: "Nữ", child: Text("Nữ",style: TextStyle(color: Colors.black))),
                ],
                onChanged: (value) {
                  setState(() {
                    _selectedGender = value!;
                  });
                },
                decoration: InputDecoration(
                  labelText: 'Giới tính',
                  labelStyle: TextStyle(color: Colors.grey.shade600, fontSize: 17),
                  floatingLabelStyle: TextStyle(color: Color.fromARGB(255, 47, 100, 253), fontSize: 18),
                  enabledBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Colors.grey.shade300),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Color.fromARGB(255, 47, 100, 253)),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  filled: true,
                  fillColor: Colors.grey.shade100,
                  prefixIcon: Icon(Icons.transgender, color: Color.fromARGB(255, 47, 100, 253)),
                ),
                style: TextStyle(fontSize: 20, color: Colors.black),
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _emailController,
                decoration: InputDecoration(
                  labelText: 'Email',
                  labelStyle: TextStyle(color: Colors.grey.shade600, fontSize: 17),
                  floatingLabelStyle: TextStyle(color: Color.fromARGB(255, 47, 100, 253), fontSize: 18),
                  enabledBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Colors.grey.shade300),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Color.fromARGB(255, 47, 100, 253)),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  errorBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Colors.red),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  filled: true,
                  fillColor: Colors.grey.shade100,
                  prefixIcon: Icon(Icons.email_outlined, color: Color.fromARGB(255, 47, 100, 253)),
                ),
                style: TextStyle(fontSize: 20),
                validator: (value) => value!.isEmpty ? 'Vui lòng nhập họ tên' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _addressController,
                decoration: InputDecoration(
                  labelText: 'Địa chỉ',
                  labelStyle: TextStyle(color: Colors.grey.shade600, fontSize: 17),
                  floatingLabelStyle: TextStyle(color: Color.fromARGB(255, 47, 100, 253), fontSize: 18),
                  enabledBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Colors.grey.shade300),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Color.fromARGB(255, 47, 100, 253)),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  errorBorder: OutlineInputBorder(
                    borderSide: BorderSide(color: Colors.red),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  filled: true,
                  fillColor: Colors.grey.shade100,
                  prefixIcon: Icon(Icons.location_on_outlined, color: Color.fromARGB(255, 47, 100, 253)),
                ),
                style: TextStyle(fontSize: 20),
                validator: (value) => value!.isEmpty ? 'Vui lòng nhập địa chỉ' : null,
              ),

              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: _updateProfile,
                style: ElevatedButton.styleFrom(
                  backgroundColor: Color.fromARGB(255, 47, 100, 253),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(50.0),
                  ),
                  padding: const EdgeInsets.symmetric(vertical: 16.0),
                  minimumSize: const Size(double.infinity, 50),
                ),
                child: const Text("Cập nhật thông tin",
                  style: TextStyle(fontSize: 19, color: Colors.white),),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
