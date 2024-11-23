import 'dart:developer';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:google_generative_ai/google_generative_ai.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart' as path;
import 'package:ungdungdatlichkham/api/api_service.dart';
import 'package:ungdungdatlichkham/constants.dart';
import 'package:ungdungdatlichkham/hive/Boxes.dart';
import 'package:ungdungdatlichkham/hive/chat_history.dart';
import 'package:ungdungdatlichkham/hive/settings.dart';
import 'package:ungdungdatlichkham/hive/user_model.dart';
import 'package:ungdungdatlichkham/models/message.dart';
import 'package:uuid/uuid.dart';

class ChatProvider extends ChangeNotifier {

  //list of messages
  final List<Message> _inChatMessages = [];

  //page controller
  final PageController _pageController = PageController();

  //index of the current screen
  int _currentIndex = 0;

  //cuttent chat id
  String _currentChatId = '';

  // initialize generative model
  GenerativeModel? _Model;

  //initialize text model
  GenerativeModel? _textModel;

  //initialize vision model
  GenerativeModel? _visionModel;

  //curent mode
  String _modelType = 'gemini-pro';

  // loading bool
  bool _isloading = false;

  //getter
  List<Message> get inChatMessages => _inChatMessages;

  PageController get pageController => _pageController;

  List<XFile>? get imagesFileList => _imagesFileList;

  int get currentIndex => _currentIndex;

  String get currentChatId => _currentChatId;

  GenerativeModel? get model => _Model;

  GenerativeModel? get textModel => _textModel;

  GenerativeModel? get visionModel => _visionModel;

  String get modelType => _modelType;

  bool get isLoading => _isloading;

  //images file list
  List<XFile>? _imagesFileList = [];

// seter
  //set in chatMessages
  Future<void> setInChatMessages({ required String chatId}) async {
    // get messages from hive database
    final messagesFromDB = await loadMessagesFromDB(chatId: chatId);

    for(var message in messagesFromDB){

      if(_inChatMessages.contains(message)){
        log('message already exists');
        continue;
      }
      _inChatMessages.add(message);
    }
    notifyListeners();
  }

  // load the messages from db
  Future<List<Message>> loadMessagesFromDB({ required String chatId}) async {
    // get messages from hive database
    //open the box of this chatID
    await Hive.openBox('${Constants.chatMessagesBox}$chatId');
    final messageBox = Hive.box('${Constants.chatMessagesBox}$chatId');

    final newData = messageBox.keys.map((e) {
      final message = messageBox.get(e);
      final messageData = Message.fromMap(Map<String, dynamic>.from(message));
      return messageData;
    }).toList();
    notifyListeners();
    return newData;
  }

  // setfile list
  void setImagesFileList({required List<XFile> listValue}){
    _imagesFileList = listValue;
    notifyListeners();
  }
  // set the current  model

  String setCurrentModel({required String newModel}){
    _modelType = newModel;
    notifyListeners();
    return newModel;
  }
//function to set the model based on bool - isTexOnly
  Future<void> setModel({required bool isTextOnly}) async{
    if (isTextOnly){
      _Model = _textModel ?? GenerativeModel(
          model: setCurrentModel(newModel: 'gemini-pro'),
          apiKey: ApiService.apiKey,);
    }else{
      _Model = _visionModel ??
            GenerativeModel(
              model: setCurrentModel(newModel: 'gemini-pro-vision'),
              apiKey: ApiService.apiKey,
            );
    }
  }
  // delete chat
  Future<void> deleteChatMessages({required String chatId}) async {
    //1. check if the box is open
    if(!Hive.isBoxOpen('${Constants.chatMessagesBox}$chatId')){
      //open the box
      await Hive.openBox('${Constants.chatMessagesBox}$chatId');
      // delete all messages in the box
      await Hive.box('${Constants.chatMessagesBox}$chatId').clear();
      // close the box
      await Hive.box('${Constants.chatMessagesBox}$chatId').close();
    }else{
      // delete all messages in the box
      await Hive.box('${Constants.chatMessagesBox}$chatId').clear();
      // close the box
      await Hive.box('${Constants.chatMessagesBox}$chatId').close();
    }
    // get the current chatId, its is not empty
    // we check if its the same as the chatID
    // if its the same we set it to empty
    if(currentChatId.isNotEmpty){
      if(currentChatId==chatId){
        setCurrentChatId(newChatId: '');
        _inChatMessages.clear();
        notifyListeners();
      }
    }
  }
  // prepare  chat room
  Future<void> prepareChatRoom({
    required bool isNewChat,
     required String chatID,}) async{
    if(!isNewChat){
      //1. load the  message from db
      final chatHistory = await loadMessagesFromDB(chatId: chatID);

      //2.clear the inchatMessages
      _inChatMessages.clear();
      for (var message in chatHistory){
        _inChatMessages.add(message);
      }
      //3. set current chat id
      setCurrentChatId(newChatId: chatID);
    }else{
      //1. clear the current chat id
      _inChatMessages.clear();

      setCurrentChatId(newChatId: chatID);
    }
  }
  //send message gemini and get the stramed reposnse
  Future<void> sentMessage({required String message,
    required bool isTextOnly,}) async{
    // set the model
    await setModel(isTextOnly: isTextOnly);

    // set loading
    setloading(value: true);

    // get the chatId
    String chatId = getChatId();

    // list of history messahes
    List<Content> history = [];
    // get the chat history
    history = await getHistory(chatId: chatId);
    // get the imagesUrls
    List<String> imagesUrls = getImagesUrls(isTextOnly: isTextOnly);

    //open the messages box
    final messagesBox =
      await Hive.openBox('${Constants.chatMessagesBox}$chatId');
      log('messageLengh:${messagesBox.keys.length}');
    // get the last user message id
    final userMessageId = messagesBox.keys.length;
    //asistan messageId
    final assistantMessageId = messagesBox.keys.length+1;

    log('userMessageId: $assistantMessageId');

    log('Model: $userMessageId');
    //user message
    final userMessage = Message(
        chatId: chatId,
        messageId: userMessageId.toString(),
        role: Role.user,
        message: StringBuffer(message),
        imagesUrls: imagesUrls,
        timeSent: DateTime.now(),
    );

    // add this message to list on inchatmessages
    _inChatMessages.add(userMessage);
    notifyListeners();
    if(currentChatId.isEmpty){
      setCurrentChatId(newChatId: chatId);
    }
    //send the message to the model and wait for the response
    await sendMessageAndWaitForResponse(
      message: message,
      chatId: chatId,
      isTextOnly: isTextOnly,
      history: history,
      userMessage: userMessage,
      modelMessageId: assistantMessageId.toString(),
        messagesBox: messagesBox,
    );
  }
  // send message to the model and wait for de response
  Future<void> sendMessageAndWaitForResponse({
    required String message,
    required String chatId,
    required bool isTextOnly,
    required List<Content> history,
    required Message userMessage,
    required String modelMessageId,
    required Box messagesBox,
}) async{
    //start the chat session - only send history is it text only
    final chatSession = _Model!.startChat(
      history: history.isEmpty || !isTextOnly ? null : history,
    );
    // get content
    final content = await getContent(message: message, isTextOnly: isTextOnly,);

    // asistant message
    final assistantMessage = userMessage.copyWith(
      messageId: modelMessageId,
      role: Role.assitant,
      message: StringBuffer(),
      timeSent: DateTime.now(),
    );
    // add this message tho the list on inChatMessages
    _inChatMessages.add(assistantMessage);
    notifyListeners();

    //wait for stream response
    chatSession.sendMessageStream(content).asyncMap((event) {
      return event;
    }).listen((event){
      _inChatMessages.firstWhere((element) =>
           element.messageId == assistantMessage.messageId &&
          element.role.name == Role.assitant.name)
          .message.write(event.text);
      log('event:${event.text}');
      notifyListeners();
    }, onDone:() async{
      log('stream done');
      // save message to hive database
      await saveMessagesToDB(
          chatID: chatId,
          userMessage: userMessage,
          assistantMessage: assistantMessage,
          messagesBox: messagesBox,
      );
      // set loading to false
      setloading(value: false);
    }).onError((erro, stackTrace){
      //setloading
      setloading(value: false);
    });
  }

  //save messages to hive db
  Future<void> saveMessagesToDB({
    required String chatID,
    required Message userMessage,
    required Message assistantMessage,
    required Box messagesBox,
  }) async{

    // the user messages
    await messagesBox.add(userMessage.toMap());
    // save the assistant messages
     await messagesBox.add(assistantMessage.toMap());
     // save chat history with th same chatid
    // if its already thre update it
    // if not create a new one
    final chatHistoryBox = Boxes.getChatHistory();
    
    final chatHistory = ChatHistory(
        chatId: chatID,
        prompt: userMessage.message.toString(),
        response:assistantMessage.message.toString(),
        imagesUrls:userMessage.imagesUrls ,
        timeStamp: DateTime.now(),
    );
    await chatHistoryBox.put(chatID, chatHistory);
    //close the box
    await messagesBox.close();
  }


  Future<Content> getContent({required String message, required bool isTextOnly,}) async{
    if(isTextOnly){
      return Content.text(message);
    }else{
      //generate text from text-only input
      final imageFutures = _imagesFileList?.map((imageFile) => imageFile.readAsBytes()).toList(growable: false);
      final imageBytes = await Future.wait(imageFutures!);
      final prompt = TextPart(message);
      final imageParts = imageBytes.map((bytes) => DataPart('image/jpeg',Uint8List.fromList(bytes))).toList();
      
      return Content.multi([prompt, ...imageParts]);
    }
  }
  //get the imagesUrls
  List<String> getImagesUrls({ required bool isTextOnly,}){
    List<String> imagesUrls = [];
    if(!isTextOnly && imagesFileList !=null){
      for(var image in imagesFileList!){
        imagesUrls.add(image.path);
      }
    }
    return imagesUrls;
  }

  Future<List<Content>> getHistory({required String chatId}) async{
    List<Content> history =[];
    if(currentChatId.isNotEmpty){
      await setInChatMessages(chatId: chatId);
      for (var message in inChatMessages){
        if(message.role == Role.user){
          history.add(Content.text(message.message.toString()));
        }else{
          history.add(Content.model([TextPart(message.message.toString())]));
        }
      }
    }
    return history;
  }
  String getChatId(){
    if(currentChatId.isEmpty){
      return const Uuid().v4();
    }else{
      return currentChatId;
    }
  }

  //set curent  page index
  void setCurrentIndex({required int newIndex}){
    _currentIndex = newIndex;
    notifyListeners();
  }
  // set current chat id
  void setCurrentChatId({required String newChatId}){
    _currentChatId = newChatId;
    notifyListeners();
  }
  //setloading

  void setloading({required bool value}){
    _isloading = value;
    notifyListeners();
  }
  //init hive box
  static initHive() async{

    final dir = await path.getApplicationDocumentsDirectory();
    Hive.init(dir.path);
    await Hive.initFlutter(Constants.geminiDB);

    //register adapters
    if(!Hive.isAdapterRegistered(0)){
      Hive.registerAdapter(ChatHistoryAdapter());
      //open the chat history box
      await Hive.openBox<ChatHistory>(Constants.ChatHistoryBox);
    }
    if(!Hive.isAdapterRegistered(0)){
      Hive.registerAdapter(UserModelAdapter());
      await Hive.openBox<UserModel>(Constants.userBox);
    }
    if(!Hive.isAdapterRegistered(2)){
      Hive.registerAdapter(SettingsAdapter());
      await Hive.openBox<Settings>(Constants.settingsBox);
    }
  }
}