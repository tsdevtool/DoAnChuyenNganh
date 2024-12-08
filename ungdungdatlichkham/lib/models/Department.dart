class Department {
  String? departmentId;
  String? name;
  String? image;

  Department({this.departmentId, this.name, this.image});

  factory Department.fromMap(Map<String, dynamic> map) {
    return Department(
      departmentId: map['department_id'],
      name: map['name'],

      image: map['image'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'department_id': departmentId,
      'name': name,

      'image': image,
    };
  }
}