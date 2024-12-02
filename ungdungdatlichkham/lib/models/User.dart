class User {
  String? userId;
  String? password;
  String? name;
  bool? sex;
  String? dateOfBirth;
  String? email;
  String? address;
  String? phone;
  String? avatar; // URL của ảnh đại diện
  int? roleId;

  User({
    this.userId,
    this.password,
    this.name,
    this.sex,
    this.dateOfBirth,
    this.email,
    this.address,
    this.phone,
    this.avatar,
    this.roleId,
  });

  factory User.fromMap(Map<String, dynamic> map) {
    return User(
      userId: map['user_id'],
      password: map['password'],
      name: map['name'],
      sex: map['sex'],
      dateOfBirth: map['date_of_birth'],
      email: map['email'],
      address: map['address'],
      phone: map['phone'],
      avatar: map['avatar'],
      roleId: map['role_id'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'user_id': userId,
      'password': password,
      'name': name,
      'sex': sex,
      'date_of_birth': dateOfBirth,
      'email': email,
      'address': address,
      'phone': phone,
      'avatar': avatar,
      'role_id': roleId,
    };
  }
}