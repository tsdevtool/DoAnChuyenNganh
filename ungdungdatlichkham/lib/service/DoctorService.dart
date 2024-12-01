import 'package:firebase_database/firebase_database.dart';
import 'dart:async';
import '../models/Doctor.dart';

class DoctorService {
  final DatabaseReference _doctorRef = FirebaseDatabase.instance.ref("doctors");
  final DatabaseReference _userRef = FirebaseDatabase.instance.ref("user");
  late StreamSubscription<DatabaseEvent> _doctorSubscription;

  final StreamController<List<Doctor>> _doctorsStreamController = StreamController<List<Doctor>>.broadcast();

  DoctorService() {
    // Đăng ký sự kiện lắng nghe thay đổi dữ liệu
    _doctorSubscription = _doctorRef.onValue.listen((event) async {
      print(">>> Dữ liệu từ bảng doctors đã thay đổi.");

      final doctorSnapshot = event.snapshot;
      if (!doctorSnapshot.exists) {
        print(">>> Không có dữ liệu trong bảng doctors.");
        _doctorsStreamController.add([]);
        return;
      }

      final doctorsMap = Map<String, dynamic>.from(doctorSnapshot.value as Map);
      print(">>> Dữ liệu doctors đã parse: $doctorsMap");

      List<Doctor> doctors = [];
      for (var entry in doctorsMap.entries) {
        final doctorData = Map<String, dynamic>.from(entry.value);
        print(">>> Đang thêm doctor với ID: ${entry.key}");
        Doctor doctor = Doctor.fromMap(entry.key, doctorData);

        // Lấy thông tin user liên quan
        if (doctor.userId != null) {
          print(">>> Đang lấy thông tin user với user_id: ${doctor.userId}");
          final userSnapshot = await _userRef.child(doctor.userId!).get();
          if (userSnapshot.exists) {
            final userData = Map<String, dynamic>.from(userSnapshot.value as Map);
            print(">>> Thông tin user: $userData");
            doctor.userName = userData['name'];
            doctor.avatarUrl = userData['avatar'];
          } else {
            print(">>> Không tìm thấy user với ID: ${doctor.userId}");
          }
        }

        doctors.add(doctor);
      }

      print(">>> Danh sách bác sĩ hoàn chỉnh: ${doctors.map((e) => e.toMap())}");
      _doctorsStreamController.add(doctors);
    });
  }

  // Hàm để truy cập Stream
  Stream<List<Doctor>> get doctorsStream => _doctorsStreamController.stream;

  // Hủy đăng ký sự kiện khi không cần thiết
  void dispose() {
    _doctorSubscription.cancel();
    _doctorsStreamController.close();
  }
}
