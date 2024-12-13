import 'package:flutter/material.dart';
import 'package:ungdungdatlichkham/Screen/DetailsDoctorScreen.dart';
import 'package:ungdungdatlichkham/models/Doctor.dart';
import 'package:ungdungdatlichkham/models/WorkSchedule.dart';
import 'package:ungdungdatlichkham/service/DoctorService.dart';
import 'package:ungdungdatlichkham/service/DepartmentService.dart';
import 'package:ungdungdatlichkham/service/WorkScheduleService.dart';
import 'FilterScreen.dart';

class DoctorListScreen extends StatefulWidget {
  const DoctorListScreen({Key? key}) : super(key: key);

  @override
  _DoctorListScreenState createState() => _DoctorListScreenState();
}

class _DoctorListScreenState extends State<DoctorListScreen> {
  final DoctorService _doctorService = DoctorService();
  final DepartmentService _departmentService = DepartmentService();
  List<Doctor> _doctors = [];
  Map<String, String> _departmentMap = {};
  String? _selectedDepartmentId;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _fetchData();
  }

  Future<void> _fetchData() async {
    final departmentMap = await _departmentService.fetchDepartmentMap();
    _doctorService.doctorsStream.listen((doctorList) {
      setState(() {
        _doctors = doctorList;
        _departmentMap = departmentMap;
        _isLoading = false;
      });
    });
  }

  List<Doctor> _applyFilters() {
    if (_selectedDepartmentId == null) {
      return _doctors;
    } else {
      return _doctors.where((doctor) => doctor.departmentId == _selectedDepartmentId).toList();
    }
  }

  Future<void> _navigateToFilterScreen() async {
    final result = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => FilterScreen(selectedDepartmentId: _selectedDepartmentId),
      ),
    );

    if (result is String?) {
      setState(() {
        _selectedDepartmentId = result;
      });
    }
  }

  Widget _buildDoctorCard(BuildContext context, Doctor doctor) {
    final departmentName = _departmentMap[doctor.departmentId] ?? 'Không rõ chuyên khoa';

    return Card(
      color: Colors.white,
      margin: const EdgeInsets.symmetric(vertical: 8.0, horizontal: 16.0),
      elevation: 4,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                ClipRRect(
                  borderRadius: BorderRadius.circular(50),
                  child: Image.network(
                    doctor.avatarUrl ?? '',
                    width: 80,
                    height: 80,
                    fit: BoxFit.cover,
                    errorBuilder: (context, error, stackTrace) => const CircleAvatar(
                      radius: 30,
                      backgroundImage: AssetImage('assets/images/default_avatar.png'),
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        doctor.userName ?? 'Bác sĩ không rõ tên',
                        style: const TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        doctor.experience ?? 'Không rõ kinh nghiệm',
                        style: const TextStyle(
                          fontSize: 17,
                          color: Colors.blueGrey,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        departmentName,
                        style: const TextStyle(
                          fontSize: 17,
                          color: Color.fromARGB(255, 47, 100, 253),
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Align(
              alignment: Alignment.centerRight,
              child: ElevatedButton(
                onPressed: () async {
                  final schedules =
                  await WorkScheduleService().getSchedulesByDoctorId(doctor.doctorId!);

                  if (schedules.isEmpty) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Bác sĩ này hiện không có lịch làm việc.')),
                    );
                    return;
                  }

                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => DoctorDetailsScreen(
                        doctor: doctor,
                        schedules: schedules,
                      ),
                    ),
                  );
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color.fromARGB(255, 47, 100, 253),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
                child: const Text(
                  'Đặt lịch ngay',
                  style: TextStyle(fontSize: 16, color: Colors.white),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final filteredDoctors = _applyFilters();

    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(
            Icons.arrow_back_ios_new,
            size: 25,
            color: Colors.white,
          ),
          onPressed: () {
            Navigator.pop(context);
          },
        ),
        backgroundColor: const Color.fromARGB(255, 47, 100, 253),
        title: const Text(
          'Danh sách bác sĩ',
          style: TextStyle(
            fontSize: 23,
            fontWeight: FontWeight.w500,
            fontFamily: 'Roboto',
            color: Colors.white
          ),
        ),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(
              Icons.filter_alt_outlined,
              color: Colors.white,
              size: 25,
            ),
            onPressed: _navigateToFilterScreen,
          ),
        ],
      ),
      backgroundColor: const Color.fromARGB(255, 211, 221, 250),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : filteredDoctors.isEmpty
          ? const Center(child: Text('Không có bác sĩ nào.'))
          : ListView.builder(
        itemCount: filteredDoctors.length,
        itemBuilder: (context, index) {
          final doctor = filteredDoctors[index];
          return _buildDoctorCard(context, doctor);
        },
      ),
    );
  }
}
