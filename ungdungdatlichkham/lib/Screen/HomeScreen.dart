import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:ungdungdatlichkham/Screen/LoginScreen.dart'; // Import màn hình đăng nhập

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[100],
      body: SafeArea(
        child: Column(
          children: [
            // Top Status Bar
            Container(
              color: Colors.blue,
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 20),
                  Row(
                    children: [
                      const FaIcon(FontAwesomeIcons.userCircle, size: 28, color: Colors.white),
                      const SizedBox(width: 10),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Buổi sáng hứng khởi!',
                            style: TextStyle(color: Colors.white),
                          ),
                          GestureDetector(
                            onTap: () {
                              // Điều hướng sang màn hình đăng nhập
                              Navigator.push(
                                context,
                                MaterialPageRoute(builder: (context) => const LoginScreen()),
                              );
                            },
                            child: const Text(
                              'Đăng ký / Đăng nhập',
                              style: TextStyle(
                                color: Colors.white,
                                fontWeight: FontWeight.bold,
                                decoration: TextDecoration.underline, // Tạo hiệu ứng underline
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),
                  // Search Input
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
            ),
            const SizedBox(height: 20),
            // Redesigned Promotion Section
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0),
              child: Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: const [
                    BoxShadow(
                      color: Colors.black12,
                      blurRadius: 10,
                      offset: Offset(0, 5),
                    ),
                  ],
                ),
                child: Stack(
                  children: [
                    ClipRRect(
                      borderRadius: BorderRadius.circular(20),
                      child: Image.asset(
                        'assets/images/datkhammomo.jpg',  // Đường dẫn đến ảnh trong thư mục assets
                        fit: BoxFit.cover,
                        width: double.infinity,
                        height: 200,
                      ),
                    ),
                    Positioned(
                      bottom: 0,
                      left: 0,
                      right: 0,
                      child: Container(
                        decoration: BoxDecoration(
                          color: Colors.black.withOpacity(0.5),
                          borderRadius: const BorderRadius.vertical(bottom: Radius.circular(20)),
                        ),
                        padding: const EdgeInsets.all(16.0),
                        child: Column(
                          children: [
                            const Text(
                              'Đặt lịch liền tay nhận ngay ưu đãi',
                              style: TextStyle(
                                color: Colors.white,
                                fontWeight: FontWeight.bold,
                                fontSize: 18,
                              ),
                              textAlign: TextAlign.center,
                            ),
                            const SizedBox(height: 10),
                            ElevatedButton(
                              onPressed: () {},
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Colors.blue,
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(20),
                                ),
                                padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 20),
                              ),
                              child: const Text('Đặt ngay'),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),
            // Icons Section
            Expanded(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: GridView.count(
                  crossAxisCount: 4,
                  childAspectRatio: 0.6,
                  mainAxisSpacing: 16,
                  crossAxisSpacing: 16,
                  children: [
                    _buildIconCard(FontAwesomeIcons.userMd, 'Đặt khám bác sĩ', Colors.orange),
                    _buildIconCard(FontAwesomeIcons.clinicMedical, 'Đặt khám phòng khám', Colors.blue),
                    _buildIconCard(FontAwesomeIcons.hospital, 'Đặt khám bệnh viện', Colors.pink),
                    _buildIconCard(FontAwesomeIcons.comments, 'Chat với bác sĩ', Colors.teal),
                    _buildIconCard(FontAwesomeIcons.video, 'Gọi video với bác sĩ', Colors.purple),
                    _buildIconCard(FontAwesomeIcons.fileMedicalAlt, 'Kết quả khám', Colors.green),
                    _buildIconCard(FontAwesomeIcons.syringe, 'Đặt lịch Tiêm chủng', Colors.pink),
                    _buildIconCard(FontAwesomeIcons.vials, 'Đặt lịch Xét nghiệm', Colors.teal),
                    _buildIconCard(FontAwesomeIcons.users, 'Cộng đồng', Colors.blue, true),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: BottomAppBar(
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          children: [
            _buildBottomNavigationBarItem(FontAwesomeIcons.home, 'Trang chủ', Colors.blue),
            _buildBottomNavigationBarItem(FontAwesomeIcons.calendarAlt, 'Lịch khám', Colors.grey),
            _buildBottomNavigationBarItem(FontAwesomeIcons.comments, 'Tin nhắn', Colors.grey),
            _buildBottomNavigationBarItem(FontAwesomeIcons.store, 'Cửa hàng', Colors.grey),
            _buildBottomNavigationBarItem(FontAwesomeIcons.user, 'Tài khoản', Colors.grey),
          ],
        ),
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

  Widget _buildBottomNavigationBarItem(IconData icon, String text, Color color) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        FaIcon(icon, color: color),
        const SizedBox(height: 4),
        Text(
          text,
          style: TextStyle(fontSize: 12, color: color),
        ),
      ],
    );
  }
}
