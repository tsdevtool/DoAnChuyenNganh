class Diagnose {
  String? diagnoseId;
  String? name;
  String? departmentId;

  Diagnose({this.diagnoseId, this.name, this.departmentId});

  factory Diagnose.fromMap(Map<String, dynamic> map) {
    return Diagnose(
      diagnoseId: map['diagnose_id'],
      name: map['name'],
      departmentId: map['department_id'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'diagnose_id': diagnoseId,
      'name': name,
      'department_id': departmentId,
    };
  }
}