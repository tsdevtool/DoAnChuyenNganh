import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:ungdungdatlichkham/Screen/Home_Screen.dart';
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
  bool _agreeToTerms = false;

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
        MaterialPageRoute(builder: (context) => const Home_Screen()),
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
      backgroundColor: Colors.white,
      body: Padding(
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
                    prefixIcon: const Icon(Icons.email_outlined, color: Color.fromARGB(255, 47, 100, 253),),
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
                    prefixIcon: const Icon(Icons.lock_outline, color: Color.fromARGB(255, 47, 100, 253),),
                    suffixIcon: IconButton(
                      icon: const Icon(Icons.visibility_outlined, color: Color.fromARGB(255, 47, 100, 253),),
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

                        }
                        );
                      },
                    ),
                    const Text("Ghi nhớ tài khoản", style: TextStyle(fontSize: 18),),
                  ],
                ),
                const SizedBox(height: 16),
                ElevatedButton(
                  onPressed: _login,
                  style: ElevatedButton.styleFrom(
                    backgroundColor:Color.fromARGB(255, 47, 100, 253),
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
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    TextButton(
                      onPressed: () {},
                      child: const Text('Quên mật khẩu?', style: TextStyle(fontSize: 18, color: Color.fromARGB(255, 47, 100, 253),),),
                    ),

                    TextButton(
                      onPressed: () {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (context) => const RegisterScreen(),
                          ),
                        );
                      },
                      child: const Text('Đăng ký tài khoản',style: TextStyle(fontSize: 18,color: Colors.deepPurpleAccent )),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                const Text(
                  "HOẶC",
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    IconButton(
                      onPressed: () {},
                      icon: const FaIcon(
                        FontAwesomeIcons.google,
                        color: Colors.red,
                        size: 35,
                      ),
                    ),
                    const SizedBox(width: 16),
                    IconButton(
                      onPressed: () {},
                      icon: const FaIcon(
                        FontAwesomeIcons.facebook,
                        color: Colors.blue,
                        size: 35,
                      ),
                    ),
                    const SizedBox(width: 16),
                    IconButton(
                      onPressed: () {},
                      icon: const FaIcon(
                        FontAwesomeIcons.apple,
                        color: Colors.black,
                        size: 35,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}


