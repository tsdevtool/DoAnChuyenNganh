class Appointment {
  int? appointmentId;
  String? userId;
  String? workScheduleId;
  String? medicalCondition;
  String? imageMedicalRecords;
  bool? status;
  String? timeSlot; // Lưu mã timeSlot khách hàng chọn
  String? patientName;
  String? phoneNumber;
  String? appointmentDate;
  String? appointmentTime;

  Appointment({
    this.appointmentId,
    this.userId,
    this.workScheduleId,
    this.medicalCondition,
    this.imageMedicalRecords,
    this.status,
    this.timeSlot,
    this.patientName,
    this.phoneNumber,
    this.appointmentDate,
    this.appointmentTime,
  });

  factory Appointment.fromMap(Map<String, dynamic> map) {
    return Appointment(
      appointmentId: map['appointment_id'],
      userId: map['user_id'],
      workScheduleId: map['work_schedule_id'],
      medicalCondition: map['medical_condition'],
      imageMedicalRecords: map['image_medical_records'],
      status: map['status'],
      timeSlot: map['timeSlot'],
      patientName: map['patientName'],
      phoneNumber: map['phoneNumber'],
      appointmentDate: map['appointmentDate'],
      appointmentTime: map['appointmentTime'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'appointment_id': appointmentId,
      'user_id': userId,
      'work_schedule_id': workScheduleId,
      'medical_condition': medicalCondition,
      'image_medical_records': imageMedicalRecords,
      'status': status,
      'timeSlot': timeSlot,
      'patientName': patientName,
      'phoneNumber': phoneNumber,
      'appointmentDate': appointmentDate,
      'appointmentTime': appointmentTime,
    };
  }
}
