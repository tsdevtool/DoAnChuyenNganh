import 'package:firebase_database/firebase_database.dart';
import 'package:bcrypt/bcrypt.dart';
import 'package:ungdungdatlichkham/models/User.dart';

class UserService {
  final DatabaseReference _userRef = FirebaseDatabase.instance.ref("user");

  Future<User?> login(String email, String password) async {
    try {
      // Lấy dữ liệu từ Firebase
      DataSnapshot snapshot = await _userRef.get();

      if (!snapshot.exists) {
        return null; // Không có dữ liệu người dùng
      }

      Map<String, dynamic> usersMap = Map<String, dynamic>.from(snapshot.value as Map);

      for (var userEntry in usersMap.entries) {
        Map<String, dynamic> userData = Map<String, dynamic>.from(userEntry.value);
        if (userData['email'] == email) {
          String hashedPassword = userData['password'];
          if (BCrypt.checkpw(password, hashedPassword)) {
            return User.fromMap(userData); // Trả về đối tượng User
          }
        }
      }

      return null; // Không tìm thấy user hoặc mật khẩu không đúng
    } catch (e) {
      print("Lỗi khi lấy dữ liệu từ Firebase: $e");
      return null;
    }
  }
}