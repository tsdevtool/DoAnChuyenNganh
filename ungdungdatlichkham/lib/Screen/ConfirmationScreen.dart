import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:ungdungdatlichkham/models/Appointment.dart';
import 'package:ungdungdatlichkham/service/AppointmentService.dart';

class ConfirmationScreen extends StatefulWidget {
  final Appointment appointment;
  final String userId;
  final String workScheduleId;
  final String phoneNumber;
  final String doctorName;

  const ConfirmationScreen({
    Key? key,
    required this.appointment,
    required this.userId,
    required this.workScheduleId,
    required this.phoneNumber,
    required this.doctorName,
  }) : super(key: key);

  @override
  _ConfirmationScreenState createState() => _ConfirmationScreenState();
}

class _ConfirmationScreenState extends State<ConfirmationScreen> {
  final TextEditingController _medicalConditionController = TextEditingController();
  final TextEditingController _phoneController = TextEditingController();
  final TextEditingController _patientNameController = TextEditingController();
  XFile? _image;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _medicalConditionController.text = widget.appointment.medicalCondition ?? "";
    _phoneController.text = widget.phoneNumber;
    _patientNameController.text = widget.appointment.patientName ?? "Không rõ";
  }

  Future<void> _pickImage() async {
    final ImagePicker picker = ImagePicker();
    final XFile? pickedFile = await picker.pickImage(source: ImageSource.gallery);
    setState(() {
      _image = pickedFile;
    });
  }

  Future<void> _uploadImageToFirebase() async {
    if (_image == null) return;

    final storageRef = FirebaseStorage.instance.ref();
    final imageRef = storageRef.child('medical_records/${_image!.name}');

    await imageRef.putFile(File(_image!.path));
    String downloadUrl = await imageRef.getDownloadURL();
    widget.appointment.imageMedicalRecords = downloadUrl;
  }

  void _confirmAppointment() async {
    if (_medicalConditionController.text.isEmpty ||
        _phoneController.text.isEmpty ||
        _patientNameController.text.isEmpty) {
      _showErrorDialog("Vui lòng nhập đầy đủ thông tin.");
      return;
    }

    setState(() {
      _isLoading = true;
    });

    widget.appointment.userId = widget.userId;
    widget.appointment.workScheduleId = widget.workScheduleId;
    widget.appointment.status = 2; // Trạng thái ban đầu là 2 (Pending)
    widget.appointment.medicalCondition = _medicalConditionController.text;
    widget.appointment.phoneNumber = _phoneController.text;
    widget.appointment.patientName = _patientNameController.text;

    try {
      await _uploadImageToFirebase();
      Appointmentservice dbService = Appointmentservice();
      await dbService.addAppointment(widget.appointment);

      // Hiển thị Snackbar khi thành công
      _showSuccessSnackbar("Đặt lịch thành công!");
    } catch (e) {
      _showErrorDialog("Có lỗi xảy ra: $e");
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  void _showErrorDialog(String message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Lỗi"),
        content: Text(message),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("OK"),
          ),
        ],
      ),
    );
  }
  void _showSuccessSnackbar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(
          children: [
            const Icon(Icons.check_circle, color: Colors.white),
            const SizedBox(width: 10),
            Expanded(
              child: Text(
                message,
                style: const TextStyle(fontSize: 16),
              ),
            ),
          ],
        ),
        backgroundColor: Colors.green,
        behavior: SnackBarBehavior.floating,
        margin: const EdgeInsets.all(16),
      ),
    );

    // Quay lại ngay lập tức sau khi hiển thị Snackbar
    Navigator.popUntil(context, (route) => route.isFirst);
  }

  void _showSuccessDialog(String message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Thành công"),
        content: Text(message),
        actions: [
          TextButton(
            onPressed: () => Navigator.popUntil(context, (route) => route.isFirst),
            child: const Text("OK"),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Xác nhận đặt lịch",
        style: TextStyle(
          fontSize: 23, fontWeight: FontWeight.bold, fontFamily: 'Rotobo'
        ),),
        leading: IconButton(
          icon: Icon(Icons.arrow_back_ios_new,
          size: 25,
          color: Color.fromARGB(255, 47, 100, 253),),
          onPressed: (){
            Navigator.pop(context);
          },
        ),
        centerTitle: true,
        backgroundColor: Colors.white,

      ),
      backgroundColor: Colors.white,
      body: Stack(
        children: [
          Column(
            children: [
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.all(20.0),
                  children: [
                    _buildDoctorInfoCard(),
                    _buildPatientInfoCard(),
                    _buildMedicalConditionCard(),
                    _buildMedicalImageCard(),
                  ],
                ),
              ),
              _buildFixedConfirmButton(),
            ],
          ),
          if (_isLoading)
            const Center(
              child: CircularProgressIndicator(),
            ),
        ],
      ),
    );
  }

  Widget _buildDoctorInfoCard() {
    return Card(
      elevation: 5,
      color: Colors.white,
      margin: const EdgeInsets.only(bottom: 20),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitleWithIcon("Thông tin bác sĩ", Icons.person),
            const SizedBox(height: 10),
            Text("Tên bác sĩ: ${widget.doctorName}", style: const TextStyle(fontSize: 18)),
            const SizedBox(height: 5),
            Text("Ngày khám: ${widget.appointment.appointmentDate}", style: const TextStyle(fontSize: 18)),
            const SizedBox(height: 5),
            Text("Giờ khám: ${widget.appointment.appointmentTime}", style: const TextStyle(fontSize: 18)),
          ],
        ),
      ),
    );
  }

  Widget _buildPatientInfoCard() {
    return Card(
      elevation: 5,
      color: Colors.white,
      margin: const EdgeInsets.only(bottom: 20),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitleWithIcon("Thông tin bệnh nhân", Icons.account_circle),
            const SizedBox(height: 10),
            TextField(
              controller: _patientNameController,
              decoration: const InputDecoration(
                labelText: "Tên bệnh nhân",
                labelStyle: TextStyle(
                  fontSize: 18
                ),
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 20),
            TextField(
              controller: _phoneController,
              keyboardType: TextInputType.phone,
              decoration: const InputDecoration(
                labelText: "Số điện thoại",
                labelStyle: TextStyle(
                  fontSize: 18
                ),
                border: OutlineInputBorder(),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMedicalConditionCard() {
    return Card(
      elevation: 5,
      color: Colors.white,
      margin: const EdgeInsets.only(bottom: 20),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitleWithIcon("Tình trạng sức khỏe", Icons.health_and_safety),
            const SizedBox(height: 10),
            TextField(
              controller: _medicalConditionController,
              decoration: const InputDecoration(
                labelText: "Tình trạng sức khỏe",
                labelStyle: TextStyle(
                  fontSize: 18
                ),
                border: OutlineInputBorder(),
              ),
              maxLines: 3,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMedicalImageCard() {
    return Card(
      elevation: 5,
      color: Colors.white,
      margin: const EdgeInsets.only(bottom: 20),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitleWithIcon("Hình ảnh hồ sơ y tế", Icons.image),
            const SizedBox(height: 10),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                ElevatedButton(
                  onPressed: _pickImage,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Color.fromARGB(255, 47, 100, 253),

                  ),
                  child: const Text("Chọn hình ảnh",
                  style: TextStyle(
                    color: Colors.white
                  ),),
                ),
              ],
            ),
            if (_image != null)
              Padding(
                padding: const EdgeInsets.only(top: 10),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      "Hình ảnh đã chọn:",
                      style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 10),
                    Image.file(
                      File(_image!.path),
                      height: 150,
                      width: 150,
                      fit: BoxFit.cover,
                    ),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildFixedConfirmButton() {
    return Container(
      color: Colors.white,
      padding: const EdgeInsets.all(16.0),
      child: SizedBox(
        width: double.infinity,
        child: ElevatedButton(
          onPressed: _confirmAppointment,
          style: ElevatedButton.styleFrom(
            padding: const EdgeInsets.symmetric(vertical: 16),
            backgroundColor: Color.fromARGB(255, 47, 100, 253),
          ),
          child: const Text(
            "Xác nhận đặt lịch",
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white, fontFamily: 'Roboto'),
          ),
        ),
      ),
    );
  }

  Widget _buildSectionTitleWithIcon(String title, IconData icon) {
    return Row(
      children: [
        Icon(icon, color: Color.fromARGB(255, 47, 100, 253), size: 30),
        const SizedBox(width: 8),
        Text(
          title,
          style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
        ),
      ],
    );
  }
}
