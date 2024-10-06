import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

class RegisterScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Đăng ký'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              'Tạo tài khoản mới',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 16),
            TextField(
              decoration: InputDecoration(
                labelText: 'Tên',
                prefixIcon: FaIcon(FontAwesomeIcons.user),
                border: OutlineInputBorder(),
              ),
            ),
            SizedBox(height: 16),
            TextField(
              keyboardType: TextInputType.phone,
              decoration: InputDecoration(
                labelText: 'Số điện thoại',
                prefixIcon: FaIcon(FontAwesomeIcons.phone),
                border: OutlineInputBorder(),
              ),
            ),
            SizedBox(height: 16),
            TextField(
              decoration: InputDecoration(
                labelText: 'Mật khẩu',
                prefixIcon: FaIcon(FontAwesomeIcons.lock),
                border: OutlineInputBorder(),
              ),
              obscureText: true,
            ),
            SizedBox(height: 16),
            ElevatedButton(
              onPressed: () {
                // Xử lý đăng ký bằng số điện thoại ở đây
              },
              child: Text('Đăng ký'),
              style: ElevatedButton.styleFrom(
                padding: EdgeInsets.symmetric(horizontal: 100, vertical: 16),
              ),
            ),
            SizedBox(height: 16),
            TextButton(
              onPressed: () {
                // Quay lại màn hình đăng nhập
                Navigator.pop(context);
              },
              child: Text('Đã có tài khoản? Đăng nhập ngay!'),
            ),
          ],
        ),
      ),
    );
  }
}
