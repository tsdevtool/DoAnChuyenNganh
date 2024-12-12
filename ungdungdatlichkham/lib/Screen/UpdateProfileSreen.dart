import 'dart:io';
import 'package:flutter/material.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:firebase_database/firebase_database.dart';
import 'package:image_picker/image_picker.dart';
import 'package:intl/intl.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:ungdungdatlichkham/models/User.dart';

class UpdateProfileScreen extends StatefulWidget {
  const UpdateProfileScreen({super.key});

  @override
  _UpdateProfileScreenState createState() => _UpdateProfileScreenState();
}

class _UpdateProfileScreenState extends State<UpdateProfileScreen> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _phoneController = TextEditingController();
  final TextEditingController _addressController = TextEditingController();
  final TextEditingController _dateController = TextEditingController(); // Controller để hiển thị ngày sinh
  String _selectedGender = "Nam";
  File? _image;
  String? _avatarUrl;
  String? _userId;
  User? _currentUser;

  final DatabaseReference _databaseRef = FirebaseDatabase.instance.ref().child('user');
  final FirebaseStorage _storage = FirebaseStorage.instance;

  @override
  void initState() {
    super.initState();
    _loadUserIdAndProfile();
  }

  Future<void> _loadUserIdAndProfile() async {
    final prefs = await SharedPreferences.getInstance();
    final userId = prefs.getString('user_id');

    if (userId != null) {
      final snapshot = await _databaseRef.child(userId).get();
      if (snapshot.exists) {
        final userData = Map<String, dynamic>.from(snapshot.value as Map);
        setState(() {
          _currentUser = User.fromMap(userData);

          // Hiển thị thông tin lên TextField
          _nameController.text = _currentUser?.name ?? '';
          _phoneController.text = _currentUser?.phone ?? '';
          _addressController.text = _currentUser?.address ?? '';
          _selectedGender = (_currentUser?.sex ?? true) ? "Nam" : "Nữ";
          if (_currentUser?.dateOfBirth != null) {
            final dateOfBirth = DateTime.parse(_currentUser!.dateOfBirth!);
            _dateController.text = DateFormat('dd/MM/yyyy').format(dateOfBirth);
          }

          // Hiển thị ảnh đại diện
          if (_currentUser?.avatar != null && _currentUser!.avatar!.isNotEmpty) {
            _avatarUrl = _currentUser!.avatar!;
          }
        });
        print("Thông tin người dùng đã được tải thành công: ${_currentUser!.toMap()}");
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
      // Chỉ cập nhật các trường đã thay đổi
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

      // Xử lý avatar nếu người dùng chọn ảnh mới
      if (_image != null) {
        final storageRef = _storage.ref().child('users/${_currentUser?.userId}/avatar.jpg');
        final uploadTask = await storageRef.putFile(_image!);
        final newAvatarUrl = await uploadTask.ref.getDownloadURL();

        if (newAvatarUrl != _currentUser?.avatar) {
          updatedData['avatar'] = newAvatarUrl;
        }
      }

      // Nếu không có gì thay đổi, thoát
      if (updatedData.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Không có thông tin nào thay đổi')),
        );
        return;
      }

      // Thực hiện cập nhật
      await _databaseRef.child(_currentUser!.userId!).update(updatedData);

      ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Cập nhật thông tin thành công!')));

      // Cập nhật thông tin hiện tại trong bộ nhớ
      _currentUser = User.fromMap({..._currentUser!.toMap(), ...updatedData});
      print("Thông tin người dùng đã được cập nhật thành công: ${_currentUser!.toMap()}");
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
                      child: const Text(
                        "Hủy",
                        style: TextStyle(fontSize: 17, color: Colors.red),
                      ),
                    ),
                    ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blue,
                      ),
                      onPressed: () {
                        if (pickedDate != null) {
                          setState(() {
                            _dateController.text = DateFormat('dd/MM/yyyy').format(pickedDate!);
                          });
                        }
                        Navigator.pop(context);
                      },
                      child: const Text(
                        "Xác nhận",
                        style: TextStyle(fontSize: 17, color: Colors.white),
                      ),
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
        title: const Text("Cập nhật thông tin cá nhân"),
      ),
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
              const SizedBox(height: 16),
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(labelText: 'Họ tên'),
                validator: (value) => value!.isEmpty ? 'Vui lòng nhập họ tên' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _phoneController,
                keyboardType: TextInputType.phone,
                decoration: const InputDecoration(labelText: 'Số điện thoại'),
                validator: (value) => value!.isEmpty ? 'Vui lòng nhập số điện thoại' : null,
              ),
              const SizedBox(height: 16),
              GestureDetector(
                onTap: () => _showCenteredDatePicker(context),
                child: AbsorbPointer(
                  child: TextFormField(
                    controller: _dateController,
                    decoration: const InputDecoration(labelText: 'Ngày sinh'),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                value: _selectedGender,
                items: const [
                  DropdownMenuItem(value: "Nam", child: Text("Nam")),
                  DropdownMenuItem(value: "Nữ", child: Text("Nữ")),
                ],
                onChanged: (value) {
                  setState(() {
                    _selectedGender = value!;
                  });
                },
                decoration: const InputDecoration(labelText: 'Giới tính'),
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _addressController,
                decoration: const InputDecoration(labelText: 'Địa chỉ'),
                validator: (value) => value!.isEmpty ? 'Vui lòng nhập địa chỉ' : null,
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: _updateProfile,
                child: const Text("Cập nhật thông tin"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}