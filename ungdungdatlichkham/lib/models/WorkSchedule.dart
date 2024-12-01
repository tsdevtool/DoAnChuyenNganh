class WorkSchedule {
  dynamic workScheduleId; // Chấp nhận cả String và int
  String? schedule;
  List<String>? timeSlots;
  String? doctorId;

  WorkSchedule({
    this.workScheduleId,
    this.schedule,
    this.timeSlots,
    this.doctorId,
  });

  factory WorkSchedule.fromMap(Map<String, dynamic> map) {
    return WorkSchedule(
      workScheduleId: map['workScheduleId'], // Không cần chuyển đổi ở đây
      schedule: map['schedule'],
      timeSlots: map['timeSlots'] != null
          ? List<String>.from(map['timeSlots'] as List)
          : [],
      doctorId: map['doctor_id'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'workScheduleId': workScheduleId,
      'schedule': schedule,
      'timeSlots': timeSlots,
      'doctor_id': doctorId,
    };
  }
}
