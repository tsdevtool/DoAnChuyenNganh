import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ungdungdatlichkham/Screen/chat_historyscreen.dart';
import 'package:ungdungdatlichkham/Screen/chat_screen.dart';
import 'package:ungdungdatlichkham/Screen/profile_screen.dart';
import 'package:ungdungdatlichkham/providers/chat_provider.dart';


class Home_Screen extends StatefulWidget {
  const Home_Screen({super.key});

  @override
  State<Home_Screen> createState() => _Home_ScreenState();
}

class _Home_ScreenState extends State<Home_Screen> {
  //list off screen
  final List<Widget> _screens =[
    const ChatHistoryscreen(),
    const chatScreen(),
    const profilescreen(),

  ];


  @override
  Widget build(BuildContext context){
    return Consumer<ChatProvider>(
      builder: (context, chatProvider,child){
      return Scaffold(
          body: PageView(
              controller: chatProvider.pageController,
              children:_screens,
              onPageChanged: (index){
                chatProvider.setCurrentIndex(newIndex: index);
              }
          ),
          bottomNavigationBar: BottomNavigationBar(
            currentIndex: chatProvider.currentIndex,
            elevation: 0,
            selectedItemColor:Theme.of(context).colorScheme.primary ,
            onTap: (index){
              chatProvider.setCurrentIndex(newIndex: index);
              chatProvider.pageController.jumpToPage(index);
            },
            items: const[
              BottomNavigationBarItem(
                icon: Icon(Icons.history),
                label: 'Chat History',),
              BottomNavigationBarItem(
                icon: Icon(Icons.chat),
                label: 'Chat',),
              BottomNavigationBarItem(
                icon: Icon(Icons.person),
                label: 'profile',),
            ],
          ));
      },
    ) ;
}
}