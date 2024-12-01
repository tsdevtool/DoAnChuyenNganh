import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:ungdungdatlichkham/Screen/LoginScreen.dart';
import 'package:ungdungdatlichkham/Screen/DetailsDoctorScreen.dart';
import 'package:ungdungdatlichkham/service/DoctorService.dart';
import 'package:ungdungdatlichkham/service/WorkScheduleService.dart';
import 'package:ungdungdatlichkham/models/Doctor.dart';
import 'package:ungdungdatlichkham/models/WorkSchedule.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  String? _userName;

  @override
  void initState() {
    super.initState();
    _loadUserName();
  }

  Future<void> _loadUserName() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    setState(() {
      _userName = prefs.getString('userName');
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[100],
      body: SafeArea(
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Top Status Bar
              _buildTopBar(),
              const SizedBox(height: 20),

              // Icons Section
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0),
                child: Wrap(
                  spacing: 16.0,
                  runSpacing: 16.0,
                  children: [
                    _buildIconCard(FontAwesomeIcons.userMd, 'Đặt khám bác sĩ', Colors.orange),
                    _buildIconCard(FontAwesomeIcons.fileMedicalAlt, 'Kết quả khám', Colors.green),
                    _buildIconCard(FontAwesomeIcons.comments, 'Chat với AI', Colors.teal),
                  ],
                ),
              ),
              const SizedBox(height: 20),

              // Danh sách bác sĩ
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Bác sĩ',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 10),
                    SizedBox(
                      height: 180,
                      child: StreamBuilder<List<Doctor>>(
                        stream: DoctorService().doctorsStream,
                        builder: (context, snapshot) {
                          if (snapshot.connectionState == ConnectionState.waiting) {
                            return const Center(child: CircularProgressIndicator());
                          } else if (snapshot.hasError) {
                            return Center(
                              child: Text(
                                'Lỗi khi tải dữ liệu: ${snapshot.error}',
                                style: const TextStyle(color: Colors.red),
                              ),
                            );
                          } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                            return const Center(
                              child: Text('Không có bác sĩ nào.'),
                            );
                          } else {
                            final doctors = snapshot.data!;
                            return ListView.builder(
                              scrollDirection: Axis.horizontal,
                              itemCount: doctors.length,
                              itemBuilder: (context, index) {
                                final doctor = doctors[index];
                                return _buildDoctorCard(context, doctor);
                              },
                            );
                          }
                        },
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),

              // Danh sách bệnh viện
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Bệnh viện',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 10),
                    _buildHospitalCard(
                      'Bệnh viện Lê Văn Thịnh',
                      '130 Lê Văn Thịnh, P. Bình Trung Tây, TP. Thủ Đức, TP.HCM',
                      'assets/images/benhvien1.jpg',
                    ),
                    const SizedBox(height: 10),
                    _buildHospitalCard(
                      'Bệnh viện Răng Hàm Mặt',
                      '263-265 Trần Hưng Đạo, P. Cô Giang, Q.1, TP.HCM',
                      'assets/images/benhvien2.jpg',
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),

              // Danh sách phòng khám
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Phòng khám',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 10),
                    _buildHospitalCard(
                      'Phòng khám Hello Doctor',
                      '152/6 Thành Thái, P.12, Q.10, TP.HCM',
                      'assets/images/benhvien3.jpg',
                    ),
                    const SizedBox(height: 10),
                    _buildHospitalCard(
                      'Shine Dental Clinic',
                      '06 Trường Sơn, P.2, Q. Tân Bình, TP.HCM',
                      'assets/images/benhvien4.jpg',
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTopBar() {
    return Container(
      color: Colors.blue,
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const SizedBox(height: 10),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  const FaIcon(FontAwesomeIcons.userCircle, size: 28, color: Colors.white),
                  const SizedBox(width: 10),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        _userName != null ? 'Chào, $_userName!' : 'Buổi tối an lành!',
                        style: const TextStyle(color: Colors.white),
                      ),
                      if (_userName == null)
                        GestureDetector(
                          onTap: () {
                            Navigator.pushReplacement(
                              context,
                              MaterialPageRoute(builder: (context) => const LoginScreen()),
                            );
                          },
                          child: const Text(
                            'Đăng ký / Đăng nhập',
                            style: TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              decoration: TextDecoration.underline,
                            ),
                          ),
                        ),
                    ],
                  ),
                ],
              ),
              const Icon(Icons.notifications, color: Colors.white),
            ],
          ),
          const SizedBox(height: 20),
          Container(
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(30),
            ),
            child: const TextField(
              decoration: InputDecoration(
                hintText: 'Tên bác sĩ, triệu chứng bệnh, chuyên khoa...',
                prefixIcon: Icon(Icons.search, color: Colors.grey),
                border: InputBorder.none,
                contentPadding: EdgeInsets.all(16),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildIconCard(IconData icon, String text, Color color, [bool isNew = false]) {
    return Column(
      children: [
        Stack(
          children: [
            Container(
              decoration: BoxDecoration(
                color: color,
                shape: BoxShape.circle,
              ),
              padding: const EdgeInsets.all(16),
              child: FaIcon(icon, color: Colors.white, size: 30),
            ),
            if (isNew)
              Positioned(
                top: 0,
                right: 0,
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 4),
                  decoration: BoxDecoration(
                    color: Colors.red,
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: const Text(
                    'Mới',
                    style: TextStyle(color: Colors.white, fontSize: 12),
                  ),
                ),
              ),
          ],
        ),
        const SizedBox(height: 8),
        Text(text, textAlign: TextAlign.center),
      ],
    );
  }

  Widget _buildDoctorCard(BuildContext context, Doctor doctor) {
    return GestureDetector(
      onTap: () async {
        List<WorkSchedule> schedules =
        await WorkScheduleService().getSchedulesByDoctorId(doctor.doctorId!);
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
      child: Container(
        width: 120,
        child: Column(
          children: [
            ClipOval(
              child: Image.network(
                doctor.avatarUrl ?? '',
                width: 80,
                height: 80,
                fit: BoxFit.cover,
                errorBuilder: (context, error, stackTrace) => const Icon(
                  Icons.person,
                  size: 80,
                  color: Colors.grey,
                ),
              ),
            ),
            const SizedBox(height: 10),
            Text(
              doctor.userName ?? 'Không rõ',
              style: const TextStyle(fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHospitalCard(String name, String address, String imageUrl) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
      elevation: 3,
      child: ListTile(
        leading: ClipRRect(
          borderRadius: BorderRadius.circular(8.0),
          child: Image.asset(imageUrl, width: 50, height: 50, fit: BoxFit.cover),
        ),
        title: Text(name),
        subtitle: Text(address),
      ),
    );
  }
}
