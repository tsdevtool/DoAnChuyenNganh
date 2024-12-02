import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:ungdungdatlichkham/service/UserService.dart';
import 'package:ungdungdatlichkham/Screen/RegisterScreen.dart';
import 'package:ungdungdatlichkham/Screen/HomeScreen.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:ungdungdatlichkham/models/User.dart';
class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  _LoginScreenState createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  final UserService _userService = UserService();

  void _login() async {
    String email = _emailController.text.trim();
    String password = _passwordController.text.trim();

    if (email.isEmpty || password.isEmpty) {
      _showErrorDialog("Vui lòng nhập đầy đủ thông tin");
      return;
    }

    final user = await _userService.login(email, password);

    if (user != null) {
      // Lưu thông tin userName và userId vào SharedPreferences
      SharedPreferences prefs = await SharedPreferences.getInstance();
      await prefs.setString('userName', user.name ?? '');
      await prefs.setString('userId', user.userId ?? '');
      await prefs.setString('phone',user.phone ?? '');
      // Đăng nhập thành công
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (context) => const HomeScreen()),
      );
    } else {
      // Đăng nhập thất bại
      _showErrorDialog("Thông tin đăng nhập không đúng. Vui lòng kiểm tra lại.");
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
            onPressed: () {
              Navigator.of(context).pop();
            },
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
        title: const Text('Đăng nhập'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text(
              'Chào mừng bạn trở lại!',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _emailController,
              keyboardType: TextInputType.emailAddress,
              decoration: const InputDecoration(
                labelText: 'Email',
                prefixIcon: FaIcon(FontAwesomeIcons.envelope),
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _passwordController,
              decoration: const InputDecoration(
                labelText: 'Mật khẩu',
                prefixIcon: FaIcon(FontAwesomeIcons.lock),
                border: OutlineInputBorder(),
              ),
              obscureText: true,
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _login,
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(horizontal: 100, vertical: 16),
              ),
              child: const Text('Đăng nhập'),
            ),
            const SizedBox(height: 16),
            TextButton(
              onPressed: () {
                // Điều hướng sang màn hình đăng ký
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const RegisterScreen()),
                );
              },
              child: const Text('Chưa có tài khoản? Đăng ký ngay!'),
            ),
          ],
        ),
      ),
    );
  }
}


