import 'package:firebase_database/firebase_database.dart';
import 'package:ungdungdatlichkham/models/WorkSchedule.dart';

class WorkScheduleService {
  final DatabaseReference _workScheduleRef =
  FirebaseDatabase.instance.ref("work_schedules");

  Future<List<WorkSchedule>> getSchedulesByDoctorId(String doctorId) async {
    final snapshot = await _workScheduleRef
        .orderByChild("doctor_id")
        .equalTo(doctorId)
        .get();

    if (!snapshot.exists || snapshot.value == null) {
      return [];
    }

    if (snapshot.value is Map) {
      final schedulesMap = snapshot.value as Map;
      return schedulesMap.entries.map((entry) {
        final scheduleData = Map<String, dynamic>.from(entry.value);
        scheduleData['workScheduleId'] =
            entry.key.toString(); // Chuyển key thành String nếu cần
        return WorkSchedule.fromMap(scheduleData);
      }).toList();
    } else if (snapshot.value is List) {
      final schedulesList = (snapshot.value as List)
          .where((element) => element != null)
          .map((element) => Map<String, dynamic>.from(element))
          .toList();
      return schedulesList.map((scheduleData) {
        return WorkSchedule.fromMap(scheduleData);
      }).toList();
    } else {
      throw Exception(
          "Dữ liệu từ Firebase không đúng định dạng: ${snapshot.value
              .runtimeType}");
    }
  }
}
