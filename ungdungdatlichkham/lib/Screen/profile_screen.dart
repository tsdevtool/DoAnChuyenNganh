import 'package:firebase_database/firebase_database.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:ungdungdatlichkham/Screen/HistoryApointmentScreen.dart';
import 'package:ungdungdatlichkham/Screen/LoginScreen.dart';
import 'package:ungdungdatlichkham/Screen/UpdatePasswordScreen.dart';
import 'package:ungdungdatlichkham/Screen/UpdateProfileSreen.dart';

import '../models/User.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({Key? key}) : super(key: key);

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final _database = FirebaseDatabase.instance.ref();
  User? _currentUser;
  bool _isLoading = true;
  @override
  void initState() {
    super.initState();
    _loadUserData();
  }
  Future<void> _loadUserData() async {
    final prefs = await SharedPreferences.getInstance();
    final userId = prefs.getString('userId') ?? ''; // Xử lý null an toàn

    if (userId.isEmpty) {
      debugPrint("Không tìm thấy userId trong SharedPreferences.");
      setState(() {
        _isLoading = false;
      });
      return;
    }

    debugPrint("Đang truy vấn dữ liệu từ Firebase với userId: $userId");
    final snapshot = await _database.child('user/$userId').get();

    if (snapshot.exists) {
      final userData = Map<String, dynamic>.from(snapshot.value as Map);
      setState(() {
        _currentUser = User.fromMap(userData);
        _isLoading = false;
      });
      debugPrint("Dữ liệu người dùng: ${_currentUser!.toMap()}");
    } else {
      debugPrint("Không tìm thấy dữ liệu người dùng.");
      setState(() {
        _isLoading = false;
      });
    }
  }

  // Hàm xử lý đăng xuất
  Future<void> _logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear(); // Xóa toàn bộ dữ liệu trong SharedPreferences
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (context) => const LoginScreen()),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    if (_currentUser == null) {
      return Center(
        child: Text(
          "Không tìm thấy thông tin người dùng.",
          style: const TextStyle(fontSize: 18, color: Colors.red),
        ),
      );
    }
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [Color.fromARGB(255, 47, 100, 253), Color(0xFF99DAF4)],
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
          ),
        ),
        child: SingleChildScrollView(
          child: Column(
            children: [
              const SizedBox(height: 70),
              // Avatar và tên người dùng
              Center(
                child: Stack(
                  alignment: Alignment.center,
                  children: [
                Container(
                decoration: BoxDecoration(
                shape: BoxShape.circle,
                  border: Border.all(
                    color: Colors.white, // Màu viền bạn muốn
                    width: 2, // Độ dày viền
                  ),
                ),
                child: CircleAvatar(
                      radius: 60,
                      backgroundImage: _currentUser?.avatar != null
                          ? NetworkImage(_currentUser!.avatar!)
                          : AssetImage('assets/images/default_avatar.png') as ImageProvider,
                      child: _currentUser?.avatar == null
                          ? Icon(Icons.person, size: 50, color: Colors.grey)
                          : null,
                    ),
                ),
                    // Button chỉnh sửa
                    Positioned(
                      bottom: 0,
                      right: 0,
                      child: GestureDetector(
                        onTap: () {
                          // Hành động khi nhấn vào icon chỉnh sửa
                        },
                        child: Container(
                          width: 45, // Kích thước hợp lý hơn
                          height: 45,
                          decoration: BoxDecoration(
                            color: Colors.white, // Màu nền trắng cho button
                            shape: BoxShape.circle, // Hình dạng tròn
                            boxShadow: [
                              BoxShadow(
                                color: Colors.grey.withOpacity(0.5), // Hiệu ứng bóng
                                spreadRadius: 2,
                                blurRadius: 5,
                              ),
                            ],
                          ),
                          child: IconButton(
                            icon: Icon(Icons.edit_outlined),
                            iconSize: 25,// Icon chỉnh sửa
                            color: Color.fromARGB(255, 139, 44, 255), // Màu xanh dương
                            onPressed: (){
                              Navigator.push(
                                context,
                                MaterialPageRoute(builder: (context) => const UpdateProfileScreen()),
                              ).then((isUpdated) {
                                if (isUpdated == true) {
                                  // Thực hiện hành động nếu thông tin đã được cập nhật
                                  _loadUserData(); // Gọi lại hàm để tải lại dữ liệu
                                }
                              });
                            },// Kích thước icon vừa phải
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 20),
              Text(
                _currentUser?.name ?? "Lỗi dữ liệu",
                style: const TextStyle(
                  fontSize: 25,
                  fontFamily: 'Roboto',
                  fontWeight: FontWeight.w600,
                  color: Colors.white,
                ),
              ),
              const SizedBox(height: 50),
              // Thông tin cá nhân
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Tiêu đề Thông tin cá nhân
                  Container(
                    margin: const EdgeInsets.symmetric(horizontal: 20),
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: const BorderRadius.only(
                        topRight: Radius.circular(20),
                        bottomRight: Radius.circular(20),
                        topLeft: Radius.circular(20),
                      ),
                      boxShadow: [
                        BoxShadow(
                          color: const Color.fromARGB(255, 47, 100, 253).withOpacity(0.2),
                          spreadRadius: 2,
                          blurRadius: 5,
                        ),
                      ],
                    ),
                    child: const Text(
                      'Thông tin cá nhân',
                      style: TextStyle(
                        fontSize: 19,
                        fontWeight: FontWeight.w500,
                        fontFamily: 'Roboto',
                        color: Color.fromARGB(255, 47, 100, 253),
                      ),
                    ),
                  ),
                  const SizedBox(height: 10),
                  // Nội dung Thông tin cá nhân
                  Container(
                    margin: const EdgeInsets.symmetric(horizontal: 20),
                    padding: const EdgeInsets.all(20),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: const BorderRadius.all(Radius.circular(20))

                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            const Icon(Icons.cake_outlined, color: Color(0xFF6C63FF), size: 30),
                            const SizedBox(width: 20),
                            Text(
                                _currentUser?.dateOfBirth ?? "Lỗi dữ liệu",
                                style: const TextStyle(fontSize: 19, fontWeight: FontWeight.w500)),
                          ],
                        ),
                        const Divider(height: 20, thickness: 0.5, color: Colors.grey),
                        Row(
                          children: [
                            const Icon(Icons.phone_outlined, color: Color(0xFF6C63FF), size: 30),
                            const SizedBox(width: 20),
                            Text( _currentUser?.phone ??"Lỗi dữ liệu",
                                style: const TextStyle(fontSize: 19, fontWeight: FontWeight.w500)),
                          ],
                        ),
                        const Divider(height: 20, thickness: 0.5, color: Colors.grey),
                        Row(
                          children: [
                            const Icon(Icons.email_outlined, color: Color(0xFF6C63FF), size: 30),
                            const SizedBox(width: 20),
                            Text( _currentUser?.email ??"Lỗi dữ liệu",
                                style: const TextStyle(fontSize: 19, fontWeight: FontWeight.w500)),
                          ],
                        ),
                        const Divider(height: 20, thickness: 0.5, color: Colors.grey),
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Icon(Icons.location_on_outlined, color: Color(0xFF6C63FF), size: 30),
                            const SizedBox(width: 20),
                            Expanded(
                              child: Text(
                                _currentUser?.address ??"Lỗi dữ liệu",
                                style: const TextStyle(fontSize: 19, fontWeight: FontWeight.w500),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 30),
                  // Tiêu đề Chức năng
                  Container(
                    margin: const EdgeInsets.symmetric(horizontal: 20),
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: const BorderRadius.only(
                        topRight: Radius.circular(20),
                        bottomRight: Radius.circular(20),
                        topLeft: Radius.circular(20),
                      ),
                      boxShadow: [
                        BoxShadow(
                          color: const Color.fromARGB(255, 47, 100, 253).withOpacity(0.2),
                          spreadRadius: 2,
                          blurRadius: 5,
                        ),
                      ],
                    ),
                    child: const Text(
                      'Chức năng',
                      style: TextStyle(
                        fontSize: 19,
                        fontWeight: FontWeight.w500,
                        fontFamily: 'Roboto',
                        color: Color.fromARGB(255, 47, 100, 253),
                      ),
                    ),
                  ),
                  const SizedBox(height: 10),
                  // Nút Chức năng
                  Container(
                    margin: const EdgeInsets.symmetric(horizontal: 20),
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: const BorderRadius.all(Radius.circular(20)),
                    ),
                    child: Column(
                      children: [
                        const SizedBox(height: 10),
                        ElevatedButton.icon(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color.fromARGB(255, 47, 100, 253),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(50),
                            ),
                            padding: const EdgeInsets.symmetric(vertical: 17),
                            minimumSize: const Size(double.infinity, 50), // Đặt chiều dài tối đa
                          ),
                          onPressed: () {
                            Navigator.push(context, MaterialPageRoute(builder: (context) => const UpdateProfileScreen()),).then((isUpdated) {
                                    if (isUpdated == true) {
                                            // Thực hiện hành động nếu thông tin đã được cập nhật
                                        _loadUserData(); // Gọi lại hàm để tải lại dữ liệu
                                    }
                            });// Kích thư
                          },
                          icon: const Icon(Icons.edit_outlined, size: 25, color: Colors.white),
                          label: const Text(
                            'Cập nhập thông tin cá nhân',
                            style: TextStyle(fontSize: 19, fontWeight: FontWeight.w500, color: Colors.white),
                          ),
                        ),
                        const SizedBox(height: 10),
                        ElevatedButton.icon(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color.fromARGB(255, 47, 100, 253),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(50),
                            ),
                            padding: const EdgeInsets.symmetric(vertical: 17),
                            minimumSize: const Size(double.infinity, 50), // Đặt chiều dài tối đa
                          ),
                          onPressed: () {
                            Navigator.push(context, MaterialPageRoute(builder: (context) => const UpdatePasswordScreen()),).then((isUpdated) {
                              if (isUpdated == true) {
                                // Thực hiện hành động nếu thông tin đã được cập nhật
                                _loadUserData(); // Gọi lại hàm để tải lại dữ liệu
                              }
                            });// Kích thư
                          },
                          icon: const Icon(Icons.key_outlined, size: 25, color: Colors.white),
                          label: const Text(
                            'Cập nhập mật khẩu',
                            style: TextStyle(fontSize: 19, fontWeight: FontWeight.w500, color: Colors.white),
                          ),
                        ),
                        const SizedBox(height: 10),
                        ElevatedButton.icon(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color.fromARGB(255, 47, 100, 253),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(50),
                            ),
                            padding: const EdgeInsets.symmetric(vertical: 17),
                            minimumSize: const Size(double.infinity, 50), // Đặt chiều dài tối đa
                          ),
                          onPressed: () {
                            // Logic xem lịch sử đặt lịch
                            Navigator.of(context).push(MaterialPageRoute(builder: (context)=> HistoryApointmentScreen(userId:  _currentUser?.userId ?? "")));
                          },
                          icon: const Icon(Icons.history_outlined, size: 25, color: Colors.white),
                          label: const Text(
                            'Xem lịch sử đặt lịch khám bệnh',
                            style: TextStyle(fontSize: 19, fontWeight: FontWeight.w500, color: Colors.white),
                          ),
                        ),
                        // const SizedBox(height: 20),
                        /*ElevatedButton.icon(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: const Color.fromARGB(255, 47, 100, 253),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(50),
                            ),
                            padding: const EdgeInsets.symmetric(vertical: 17),
                            minimumSize: const Size(double.infinity, 50), // Đặt chiều dài tối đa
                          ),
                          onPressed: () {
                            // Logic xem kết quả khám
                          },
                          icon: const Icon(Icons.receipt_outlined, size: 25, color: Colors.white),
                          label: const Text(
                            'Kết quả khám',
                            style: TextStyle(fontSize: 18, fontWeight: FontWeight.w500, color: Colors.white),
                          ),
                        ),*/
                        const SizedBox(height: 10),
                      ],
                    ),
                  ),

                ],
              ),
              const SizedBox(height: 30),
              // Nút Đăng xuất
              ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: Color.fromARGB(255, 253, 47, 47),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(50),
                  ),
                  padding:
                  const EdgeInsets.symmetric(horizontal: 40, vertical: 15),
                ),
                onPressed: _logout,

                icon: const Icon(Icons.login_rounded, size: 25, color: Colors.white, ),
                label: const Text(
                  'Đăng xuất tài khoản',
                  style: TextStyle(fontSize: 19, fontWeight: FontWeight.w500, color: Colors.white),
                ),

              ),
              const SizedBox(height: 30),
            ],
          ),
        ),
      ),
    );
  }
}
