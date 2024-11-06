import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ungdungdatlichkham/main.dart';
import 'package:ungdungdatlichkham/providers/chat_provider.dart';
import '../hive/chat_history.dart';
import '../utility/utilites.dart';


class ChatHistoryWidget extends StatelessWidget{
  const ChatHistoryWidget({
    super.key,
    required this.chat,
  });
  final ChatHistory chat;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        contentPadding: EdgeInsets.only(left: 10.0,right: 10.0),
        leading: const CircleAvatar(
          radius: 30,
          child: const Icon(Icons.chat),),
        title: Text(chat.prompt, maxLines: 1,),
        subtitle: Text(chat.response, maxLines: 2,),
        trailing: const Icon(Icons.arrow_forward_ios),
        onTap: () async{
          // navigate to chat screen
          final chatProvider = context.read<ChatProvider>();
          //prepare the chat room
          await  chatProvider.prepareChatRoom(
              isNewChat: false,
              chatID: chat.chatId,
          );
          chatProvider.setCurrentIndex(newIndex: 1);
          chatProvider.pageController.jumpToPage(1);
        },
        onLongPress: (){
          // show my animated dialog to delete the chat
          showMyAnimatedDialog(
            context: context,
            title: 'Delete Chat',
            content: 'Are you sure you want to delete this chat',
            actionText: 'Delete',
            onActionPressed: (value) async {
              if(value){
                //delete the chat
                await context.read<ChatProvider>().deleteChatMessages(chatId: chat.chatId);
                // delete the chat history
                await chat.delete();
              }
            },
          );
        },
      ),
    );
  }
}
