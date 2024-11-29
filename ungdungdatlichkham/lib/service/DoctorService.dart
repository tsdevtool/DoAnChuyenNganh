import 'package:firebase_database/firebase_database.dart';
import '../models/Doctor.dart';

class DoctorService {
  // Tham chiếu đến nhánh "doctors" trong Realtime Database
  final DatabaseReference _doctorRef = FirebaseDatabase.instance.ref("doctors");

  // Hàm lấy danh sách bác sĩ
  Future<List<Doctor>> getDoctors() async {
    try {
      // Lấy dữ liệu từ Realtime Database
      final snapshot = await _doctorRef.get();

      // Kiểm tra nếu không có dữ liệu
      if (!snapshot.exists) {
        return [];
      }

      // Chuyển dữ liệu từ snapshot thành danh sách Doctor
      final doctors = (snapshot.value as Map).values.map((doctorData) {
        return Doctor.fromMap(Map<String, dynamic>.from(doctorData));
      }).toList();

      return doctors;
    } catch (e) {
      throw Exception("Lỗi khi lấy dữ liệu bác sĩ: $e");
    }
  }
}