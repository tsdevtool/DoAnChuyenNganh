import 'package:firebase_database/firebase_database.dart';
import 'package:ungdungdatlichkham/models/Department.dart';

class DepartmentService {
  final DatabaseReference _databaseReference =
  FirebaseDatabase.instance.ref('departments');

  Future<List<Department>> getAllDepartments() async {
    try {
      final snapshot = await _databaseReference.get();
      if (snapshot.exists) {
        final Map<String, dynamic> departmentsData =
        Map<String, dynamic>.from(snapshot.value as Map);
        return departmentsData.values
            .map((e) => Department.fromMap(Map<String, dynamic>.from(e)))
            .toList();
      } else {
        return [];
      }
    } catch (error) {
      print("Lỗi khi lấy dữ liệu từ Firebase: $error");
      return [];
    }
  }
  Future<Map<String, String>> fetchDepartmentMap() async {
    final snapshot = await _databaseReference.get();

    if (snapshot.exists) {
      final data = Map<String, dynamic>.from(snapshot.value as Map);
      return data.map((key, value) {
        final department = Map<String, dynamic>.from(value);
        return MapEntry(key, department['name'] ?? 'Không rõ');
      });
    } else {
      return {};
    }
  }
}