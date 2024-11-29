class WorkSchedule {
  int? workScheduleId;
  String? schedule;
  List<String>? timeSlots; // Danh sách các khung giờ
  int? doctorId;

  WorkSchedule({
    this.workScheduleId,
    this.schedule,
    this.timeSlots,
    this.doctorId,
  });

  // Từ Map (Firebase) sang WorkSchedule
  factory WorkSchedule.fromMap(Map<String, dynamic> map) {
    return WorkSchedule(
      workScheduleId: map['work_schedule_id'],
      schedule: map['schedule'],
      timeSlots: List<String>.from(map['timeSlots'] ?? []),
      doctorId: map['doctor_id'],
    );
  }

  // Từ WorkSchedule sang Map (Firebase)
  Map<String, dynamic> toMap() {
    return {
      'work_schedule_id': workScheduleId,
      'schedule': schedule,
      'timeSlots': timeSlots,
      'doctor_id': doctorId,
    };
  }
}