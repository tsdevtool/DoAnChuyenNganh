import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ungdungdatlichkham/Screen/chat_historyscreen.dart';
import 'package:ungdungdatlichkham/providers/chat_provider.dart';
import 'package:ungdungdatlichkham/utility/utilites.dart';
import 'package:ungdungdatlichkham/widgets/bottom_chat_field.dart';


import '../widgets/chat_messages.dart';

class chatScreen extends StatefulWidget {
  final bool isInDashboard;
  const chatScreen({Key? key, this.isInDashboard=false}) : super(key:key);

@override
State<chatScreen> createState() => _chatScreenState();
}

class _chatScreenState extends State<chatScreen> {
  //scroll controller
  final ScrollController _scrollController = ScrollController();
  @override
  void initState(){
    super.initState();
  }
  @override
  void dispose(){
  _scrollController.dispose();
    super.dispose();
  }
  void _scrollToBottom(){
    WidgetsBinding.instance.addPostFrameCallback((_){
      if(_scrollController.hasClients && _scrollController.position.maxScrollExtent > 0.0){
        _scrollController.animateTo(
            _scrollController.position.maxScrollExtent,
            duration: const Duration(milliseconds: 300) ,
            curve: Curves.easeOut
        );
      }
    });
  }
@override
Widget build(BuildContext context) {
  return Consumer<ChatProvider>(
    builder: (context, chatProvider, child) {
      if(chatProvider.inChatMessages.isNotEmpty){
        _scrollToBottom();
      }
      // auto scroll to bootom on new message
      chatProvider.addListener((){
        if(chatProvider.inChatMessages.isNotEmpty){
          _scrollToBottom();
        }
      });

      return Scaffold(
        appBar: AppBar(
          centerTitle: true,
          backgroundColor:Colors.white,
          title: const Text('Hỏi đáp cùng Viet Duc AI',
          style: TextStyle(fontFamily: 'Roboto')),
          leading: widget.isInDashboard
              ? null
              : IconButton(
                  icon: const Icon(Icons.arrow_back_ios_new),
                  onPressed: () {
                    Navigator.pop(context);
                  },
          ), // Đảm bảo font hỗ trợ tiếng Việt),
          actions: [
            if(chatProvider.inChatMessages.isNotEmpty)
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: CircleAvatar(
                  child: IconButton(
                      icon: const Icon(Icons.add),
                    onPressed: () async {
                      showDialog(
                        context: context,
                        builder: (BuildContext context) {
                          return AlertDialog(
                            title: const Text('Bắt đầu đoạn chat mới',
                              style: TextStyle(fontFamily: 'Roboto'),),
                            content: const Text('Bạn có chắc muốn bắt đầu đoạn chat mới?',
                              style: TextStyle(fontFamily: 'Roboto'),),
                            actions: <Widget>[
                              TextButton(
                                onPressed: () {
                                  Navigator.of(context).pop(); // Đóng hộp thoại khi nhấn "Hủy"
                                },
                                child: const Text('Hủy'),
                              ),
                              TextButton(
                                onPressed: () async {
                                  Navigator.of(context).pop(); // Đóng hộp thoại khi nhấn "Đồng ý"
                                  // Chuẩn bị phòng chat
                                  await chatProvider.prepareChatRoom(
                                    isNewChat: true,
                                    chatID: '',
                                  );
                                },
                                child: const Text(
                                  'Đồng ý',
                                  style: TextStyle(
                                    color: Colors.blue, // Đặt màu chữ để dễ nhìn thấy
                                    fontFamily: 'Roboto',
                                  ),
                                ),
                              ),
                            ],
                          );
                        },
                      );
                      },
                  ),
                ),
              )
          ],
        ),
        body: SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(8.0),
            child: Column(
              children: [
                Expanded(
                  child:
                  chatProvider.inChatMessages.isEmpty
                      ? const Center(
                    child: Text('Chưa có tin nhắn nào!'),
                  )
                      :ChatMessages(
                    scrollController: _scrollController,
                    chatProvider: chatProvider,)
                ),
              //input field
              BottomChatField(
                chatProvider: chatProvider,)
              ],
            ),
          ),
        ),
      );
    },
  );
}
}

