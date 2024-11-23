
import 'package:hive/hive.dart';
import 'package:ungdungdatlichkham/constants.dart';
import 'package:ungdungdatlichkham/hive/chat_history.dart';
import 'package:ungdungdatlichkham/hive/settings.dart';
import 'package:ungdungdatlichkham/hive/user_model.dart';

class Boxes{
  // get the chat history box
  static Box<ChatHistory> getChatHistory() =>
      Hive.box<ChatHistory>(Constants.ChatHistoryBox);

  // get user box
  static Box<UserModel> getUser() => Hive.box(Constants.userBox);

  // get setting box
  static Box<Settings> getSettings() => Hive.box<Settings>(Constants.settingsBox);
}