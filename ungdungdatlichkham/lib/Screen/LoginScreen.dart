import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:ungdungdatlichkham/Screen/Home_Screen.dart';
import 'package:ungdungdatlichkham/service/UserService.dart';
import 'package:ungdungdatlichkham/Screen/RegisterScreen.dart';
import 'package:shared_preferences/shared_preferences.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  _LoginScreenState createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  final UserService _userService = UserService();
  bool _agreeToTerms = false;
  bool _isLoading = false;

  void _login() async {
    String email = _emailController.text.trim();
    String password = _passwordController.text.trim();

    if (email.isEmpty || password.isEmpty) {
      _showErrorDialog("Vui lòng nhập đầy đủ thông tin");
      return;
    }

    setState(() {
      _isLoading = true; // Bắt đầu hiển thị ProgressBar
    });

    final user = await _userService.login(email, password);

    if (user != null) {
      // Lưu thông tin userName và userId vào SharedPreferences
      SharedPreferences prefs = await SharedPreferences.getInstance();
      await prefs.setString('userName', user.name ?? '');
      await prefs.setString('userId', user.userId ?? '');
      await prefs.setString('phone', user.phone ?? '');

      setState(() {
        _isLoading = false; // Dừng hiển thị ProgressBar
      });

      // Hiển thị thông báo thành công
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: const Text("Đăng nhập thành công!"),
          backgroundColor: Colors.green, // Đổi màu thành xanh lá
        ),
      );

      // Đăng nhập thành công, chuyển sang trang Home_Screen
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (context) => const Home_Screen()),
      );
    } else {
      setState(() {
        _isLoading = false; // Dừng hiển thị ProgressBar
      });

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
      backgroundColor: Colors.white,
      body: Stack(
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Center(
              child: SingleChildScrollView(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Image.asset(
                      "assets/images/logo.png",
                      width: 80,
                      height: 80,
                      fit: BoxFit.cover,
                    ),
                    const SizedBox(height: 16),
                    const Text(
                      'Đăng nhập',
                      style: TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 32),
                    TextField(
                      controller: _emailController,
                      keyboardType: TextInputType.emailAddress,
                      decoration: InputDecoration(
                        labelText: 'Email',
                        hintText: 'Nhập email của bạn',
                        prefixIcon: const Icon(
                          Icons.email_outlined,
                          color: Color.fromARGB(255, 47, 100, 253),
                        ),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(20.0),
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      controller: _passwordController,
                      obscureText: true,
                      decoration: InputDecoration(
                        labelText: 'Mật khẩu',
                        hintText: '********',
                        prefixIcon: const Icon(
                          Icons.lock_outline,
                          color: Color.fromARGB(255, 47, 100, 253),
                        ),
                        suffixIcon: IconButton(
                          icon: const Icon(
                            Icons.visibility_outlined,
                            color: Color.fromARGB(255, 47, 100, 253),
                          ),
                          onPressed: () {},
                        ),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(20.0),
                        ),
                      ),
                    ),
                    const SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.start,
                      children: [
                        Checkbox(
                          value: _agreeToTerms,
                          onChanged: (value) {
                            setState(() {
                              _agreeToTerms = value!;
                            });
                          },
                        ),
                        const Text(
                          "Ghi nhớ tài khoản",
                          style: TextStyle(fontSize: 18),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: _login,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color.fromARGB(255, 47, 100, 253),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(50.0),
                        ),
                        padding: const EdgeInsets.symmetric(vertical: 16.0),
                        minimumSize: const Size(double.infinity, 50),
                      ),
                      child: const Text(
                        'Đăng nhập',
                        style: TextStyle(fontSize: 19, color: Colors.white),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
          if (_isLoading)
            Container(
              color: Colors.black.withOpacity(0.5), // Nền mờ
              child: const Center(
                child: CircularProgressIndicator(
                  color: Color.fromARGB(255, 47, 100, 253), // Màu xanh nước
                ),
              ),
            ),
        ],
      ),
    );
  }
}
