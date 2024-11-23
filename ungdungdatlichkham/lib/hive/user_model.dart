import 'package:hive/hive.dart';

part 'user_model.g.dart';
@HiveType(typeId: 1)
class UserModel extends HiveObject{

  @HiveField(0)
  final String UId;


  @HiveField(1)
  final String name;

  @HiveField(2)
  final String image;

  //contructtor
  UserModel(
      this.UId, this.name, this.image,);
}