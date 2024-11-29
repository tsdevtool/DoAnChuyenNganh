import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:ungdungdatlichkham/Screen/HomeScreen.dart';
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
  // List of screens
  final List<Widget> _screens = [
    const ChatHistoryscreen(),
    const chatScreen(),
    const profilescreen(),
    const HomeScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Consumer<ChatProvider>(
      builder: (context, chatProvider, child) {
        return Scaffold(
          body: PageView(
            controller: chatProvider.pageController,
            children: _screens,
            onPageChanged: (index) {
              chatProvider.setCurrentIndex(newIndex: index);
            },
          ),
          bottomNavigationBar: Container(
            decoration: BoxDecoration(
              color: Colors.white, // Background color
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.shade300,
                  blurRadius: 10,
                  offset: Offset(0, -3), // Effect for shadow
                ),
              ],
            ),
            child: BottomNavigationBar(
              currentIndex: chatProvider.currentIndex,
              elevation: 0,
              selectedItemColor: Theme.of(context).colorScheme.primary,
              unselectedItemColor: Colors.grey,
              showSelectedLabels: true,
              showUnselectedLabels: true,
              type: BottomNavigationBarType.fixed,
              iconSize: 28, // Larger icon size
              selectedLabelStyle: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 14,
              ),
              unselectedLabelStyle: const TextStyle(
                fontSize: 12,
              ),
              onTap: (index) {
                chatProvider.setCurrentIndex(newIndex: index);
                chatProvider.pageController.jumpToPage(index);
              },
              items: const [
                BottomNavigationBarItem(
                  icon: Icon(Icons.history),
                  label: 'Lịch sử',
                ),
                BottomNavigationBarItem(
                  icon: Icon(Icons.chat),
                  label: 'Tin nhắn',
                ),
                BottomNavigationBarItem(
                  icon: Icon(Icons.person),
                  label: 'Hồ sơ',
                ),
                BottomNavigationBarItem(
                  icon: Icon(Icons.home),
                  label: 'Trang chủ',
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}