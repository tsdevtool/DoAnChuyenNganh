import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:ungdungdatlichkham/Screen/LoginScreen.dart';
import 'package:ungdungdatlichkham/Screen/DetailsDoctorScreen.dart';
import 'package:ungdungdatlichkham/service/DoctorService.dart';
import 'package:ungdungdatlichkham/service/WorkScheduleService.dart';
import 'package:ungdungdatlichkham/models/Doctor.dart';
import 'package:ungdungdatlichkham/models/WorkSchedule.dart';

import '../models/Department.dart';
import '../service/DepartmentService.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final DepartmentService _departmentService = DepartmentService();
  List<Department> _departments = [];
  bool _isLoading = true;

  String? _userName;

  @override
  void initState() {
    super.initState();
    _loadUserName();
    _fetchDepartments();

  }
  Future<void> _fetchDepartments() async {
    final departments = await _departmentService.getAllDepartments();
    print("Danh sách chuyên khoa: $departments");
    setState(() {
      _departments = departments;
      _isLoading = false;
    });
  }



  Future<String> _fetchDepartmentName(String departmentId) async {
    final ref = FirebaseDatabase.instance.ref("departments/$departmentId");
    final snapshot = await ref.get();

    if (snapshot.exists) {
      final data = snapshot.value as Map<dynamic, dynamic>;
      return data["name"] ?? "Không rõ chuyên khoa";
    }
    return "Không rõ chuyên khoa";
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
      backgroundColor: Colors.white,
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
                    _buildFeatureGrid(),
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
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'ĐẶT KHÁM NHANH',
                          style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                        ),
                        TextButton(onPressed: (){
                          //chuyển hướng đến danh sách tất cả các bác sĩ

                        },
                            child: Text(
                              'Xem thêm',
                              style: TextStyle(
                                color: Colors.lightBlue,
                                fontSize: 20
                              ),
                            ))
                      ],
                    ),
                    const SizedBox(height: 10),
                    SizedBox(
                      height: 350,
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
                      style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
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
                      style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
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
      decoration: BoxDecoration(
        color: Colors.purple.shade100,
        borderRadius: const BorderRadius.only(
          bottomLeft: Radius.circular(30),
          bottomRight: Radius.circular(30),
        ),

      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 30),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const SizedBox(height: 10),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  const FaIcon(FontAwesomeIcons.userCircle, size: 28, color: Colors.blue),
                  const SizedBox(width: 10),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        _userName != null ? 'Chào, $_userName!' : 'Buổi tối an lành!',
                        style: const TextStyle(color: Colors.indigoAccent, fontSize: 20),
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
                              color: Colors.blueGrey,
                              fontSize: 19,
                              fontWeight: FontWeight.bold,
                              decoration: TextDecoration.underline,
                            ),
                          ),
                        ),
                    ],
                  ),
                ],
              ),
              const Icon(Icons.notifications_outlined, color: Colors.blue),
            ],
          ),
          const SizedBox(height: 40),
          Container(
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(30),
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.withOpacity(0.2),
                  spreadRadius: 1,
                  blurRadius: 5,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: TextField(
              decoration: InputDecoration(
                hintText: "Nhập tên bác sĩ, chuyên khoa...",
                hintStyle: TextStyle(color: Colors.grey.shade400),
                prefixIcon: const Icon(
                  Icons.search,
                  color: Colors.purple,
                ),
                suffixIcon: Icon(
                  Icons.mic_none,
                  color: Colors.purple.shade200,
                ),
                border: InputBorder.none,
                contentPadding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
              ),
            ),
          ),
        ],
      ),
    );
  }


  Widget _buildFeatureCard(IconData icon, String text, Color bgColor) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8), // Bo góc nhẹ hơn
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.16),
            blurRadius: 4,
            spreadRadius: 1,
            offset: const Offset(0, 2), // Đổ bóng nhỏ hơn
          ),
        ],
      ),
      padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 8), // Giảm padding // Giảm padding
      child: Row(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(8), // Giảm padding icon
            decoration: BoxDecoration(
              color: bgColor,
              borderRadius: BorderRadius.circular(6),
            ),
            child: Icon(icon, color: Colors.white, size: 24), // Icon nhỏ hơn
          ),
          const SizedBox(width: 12), // Khoảng cách nhỏ giữa icon và text
          Expanded(
            child: Text(
              text,
              style: const TextStyle(
                fontSize: 14, // Giảm font size
                fontWeight: FontWeight.w500,
                color: Colors.black,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFeatureGrid() {
    return Padding(

      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8), // Thu nhỏ padding ngoài
      child: GridView.count(
        crossAxisCount: 2, // 2 cột
        crossAxisSpacing: 8, // Khoảng cách ngang giữa các mục
        mainAxisSpacing: 8, // Khoảng cách dọc giữa các mục
        childAspectRatio: 3.12, // Tỷ lệ chiều rộng / chiều cao, tăng lên để giảm chiều cao mục
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        children: [
          _buildFeatureCard(Icons.calendar_today_outlined, 'Đặt lịch khám bệnh', Colors.blue),
          _buildFeatureCard(Icons.assignment_outlined, 'Xem kết quả', Colors.pink),
          _buildFeatureCard(Icons.chat_outlined, 'Chat với AI', Colors.orange),
          _buildFeatureCard(Icons.history_outlined, 'Lịch sử khám bệnh', Colors.teal),
        ],
      ),
    );
  }


  Widget _buildDoctorCard(BuildContext context, Doctor doctor) {
    return FutureBuilder<String>(
      future: _fetchDepartmentName(doctor.departmentId ?? ""),
      builder: (context, snapshot) {
        String departmentName = "Không rõ chuyên khoa";
        if (snapshot.connectionState == ConnectionState.waiting) {
          departmentName = "Đang tải...";
        } else if (snapshot.hasData) {
          departmentName = snapshot.data!;
        }

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
            width: 160, // Độ rộng phù hợp
            margin: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 10.0),
            padding: const EdgeInsets.all(12.0),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(12),
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.withOpacity(0.2),
                  blurRadius: 8,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                // Ảnh đại diện bác sĩ
                ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: Image.network(
                    doctor.avatarUrl ?? '',
                    width: 100,
                    height: 100,
                    fit: BoxFit.cover,
                    errorBuilder: (context, error, stackTrace) => Container(
                      width: 100,
                      height: 100,
                      color: Colors.grey[200],
                      child: const Icon(
                        Icons.person,
                        size: 50,
                        color: Colors.grey,
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 10),
                // Tên bác sĩ
                Text(
                  doctor.userName ?? 'Không rõ tên',
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 18,
                    color: Colors.black,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 5),
                // Tên chuyên khoa
                Text(
                  departmentName,
                  style: const TextStyle(
                    fontSize: 16,
                    color: Colors.blueGrey,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 10),
                // Nút đặt lịch
                ElevatedButton(
                  onPressed: () async {
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
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.blue,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  child: const Text(
                    'Đặt lịch ngay',
                    style: TextStyle(color: Colors.white),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }


  /*Widget _buildHospitalCard(String name, String address, String imageUrl) {
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
  }*/
  Widget _buildHospitalCard(String name, String address, String imageUrl) {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 8.0), // Khoảng cách giữa các thẻ
      padding: const EdgeInsets.all(12.0), // Khoảng cách bên trong thẻ
      decoration: BoxDecoration(
        color: Colors.white70, // Nền màu trắng
        borderRadius: BorderRadius.circular(10), // Bo góc
        border: Border.all(
          color: Colors.blueAccent.withOpacity(0.3), // Viền mỏng màu xám nhạt
          width: 2, // Độ rộng viền
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.2), // Màu bóng nhạt
            blurRadius: 6, // Độ mờ của bóng
            offset: const Offset(0, 2), // Độ dịch chuyển của bóng
          ),
        ],
      ),
      child: Row(
        children: [
          // Ảnh đại diện
          ClipRRect(
            borderRadius: BorderRadius.circular(5.0), // Bo góc ảnh
            child:  Image.asset(imageUrl, width: 50, height: 50, fit: BoxFit.cover),
          ),
          const SizedBox(width: 12), // Khoảng cách giữa ảnh và nội dung
          // Nội dung
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  name,
                  style: const TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.bold,
                    color: Colors.black,
                  ),
                ),
                const SizedBox(height: 4), // Khoảng cách giữa tên và địa chỉ
                Text(
                  address,
                  style: const TextStyle(
                    fontSize: 16,
                    color: Colors.grey,
                  ),
                  maxLines: 2, // Hiển thị tối đa 2 dòng
                  overflow: TextOverflow.ellipsis, // Dấu "..." nếu text quá dài
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }





}
