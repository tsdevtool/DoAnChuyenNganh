
import 'package:ungdungdatlichkham/models/WorkSchedule.dart';

class Doctor {
  int? doctorId;
  String? userId;
  int? positionId;
  String? information;
  String? experience;
  String? education;
  int? departmentId;
  String? departmentName;
  String? positionName;
  String? userName;
  String? avatarUrl;
  List<WorkSchedule>? workSchedules;

  Doctor({
    this.doctorId,
    this.userId,
    this.positionId,
    this.information,
    this.experience,
    this.education,
    this.departmentId,
    this.departmentName,
    this.positionName,
    this.userName,
    this.avatarUrl,
    this.workSchedules,
  });

  factory Doctor.fromMap(Map<String, dynamic> map) {
    return Doctor(
      doctorId: map['doctor_id'],
      userId: map['user_id'],
      positionId: map['position_id'],
      information: map['information'],
      experience: map['experience'],
      education: map['education'],
      departmentId: map['department_id'],
      departmentName: map['departmentName'],
      positionName: map['positionName'],
      userName: map['userName'],
      avatarUrl: map['avatarUrl'],
      workSchedules: (map['workSchedules'] as List<dynamic>?)
          ?.map((schedule) => WorkSchedule.fromMap(schedule))
          .toList(),
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'doctor_id': doctorId,
      'user_id': userId,
      'position_id': positionId,
      'information': information,
      'experience': experience,
      'education': education,
      'department_id': departmentId,
      'departmentName': departmentName,
      'positionName': positionName,
      'userName': userName,
      'avatarUrl': avatarUrl,
      'workSchedules': workSchedules?.map((e) => e.toMap()).toList(),
    };
  }
}
