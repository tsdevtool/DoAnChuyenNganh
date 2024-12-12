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
}