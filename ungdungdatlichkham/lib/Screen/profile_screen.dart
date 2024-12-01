import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:ungdungdatlichkham/Screen/LoginScreen.dart';

class profilescreen extends StatefulWidget {
  const profilescreen({super.key});

  @override
  State<profilescreen> createState() => _profilescreenState();
}

class _profilescreenState extends State<profilescreen> {
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
    return Scaffold(
      appBar: AppBar(
        title: const Text("Hồ sơ cá nhân"),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: _logout, // Gọi hàm đăng xuất
          ),
        ],
      ),
      body: const Center(
        child: Text('Màn hình hồ sơ cá nhân'),
      ),
    );
  }
}
