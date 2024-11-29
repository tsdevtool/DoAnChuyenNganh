class Position {
  int? positionId;
  String? name;

  Position({this.positionId, this.name});

  factory Position.fromMap(Map<String, dynamic> map) {
    return Position(
      positionId: map['position_id'],
      name: map['name'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'position_id': positionId,
      'name': name,
    };
  }
}
