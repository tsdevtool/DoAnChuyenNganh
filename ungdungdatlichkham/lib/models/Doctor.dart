class Doctor {
  String? doctorId;
  String? userId;
  String? positionId;
  String? information;
  String? experience;
  String? education;
  String? departmentId;
  String? userName; // Tên từ bảng users
  String? avatarUrl; // Ảnh từ bảng users

  Doctor({
    this.doctorId,
    this.userId,
    this.positionId,
    this.information,
    this.experience,
    this.education,
    this.departmentId,
    this.userName,
    this.avatarUrl,
  });

  factory Doctor.fromMap(String key, Map<String, dynamic> map) {
    return Doctor(
      doctorId: key, // Key từ Firebase
      userId: map['user_id'],
      positionId: map['position_id']?.toString(),
      information: map['information'],
      experience: map['experience'],
      education: map['education'],
      departmentId: map['department_id'],
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
      'user_name': userName,
      'avatar_url': avatarUrl,
    };
  }
}
