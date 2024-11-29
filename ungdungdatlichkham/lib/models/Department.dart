class Department {
  int? departmentId;
  String? name;
  int? diagnoseId;
  String? image;

  Department({this.departmentId, this.name, this.diagnoseId, this.image});

  factory Department.fromMap(Map<String, dynamic> map) {
    return Department(
      departmentId: map['department_id'],
      name: map['name'],
      diagnoseId: map['diagnose_id'],
      image: map['image'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'department_id': departmentId,
      'name': name,
      'diagnose_id': diagnoseId,
      'image': image,
    };
  }
}
