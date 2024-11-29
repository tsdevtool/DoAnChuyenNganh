class Role {
  int? roleId;
  String? name;

  Role({this.roleId, this.name});

  factory Role.fromMap(Map<String, dynamic> map) {
    return Role(
      roleId: map['role_id'],
      name: map['name'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'role_id': roleId,
      'name': name,
    };
  }
}
