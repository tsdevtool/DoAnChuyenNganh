class Diagnose {
  int? diagnoseId;
  String? name;
  int? treatmentId;

  Diagnose({this.diagnoseId, this.name, this.treatmentId});

  factory Diagnose.fromMap(Map<String, dynamic> map) {
    return Diagnose(
      diagnoseId: map['diagnose_id'],
      name: map['name'],
      treatmentId: map['treatment_id'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'diagnose_id': diagnoseId,
      'name': name,
      'treatment_id': treatmentId,
    };
  }
}
