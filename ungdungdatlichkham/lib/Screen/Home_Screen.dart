import 'package:flutter/material.dart';
import 'package:google_nav_bar/google_nav_bar.dart';
import 'package:provider/provider.dart';
import 'package:ungdungdatlichkham/Screen/HistoriesScreen.dart';
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
    const HomeScreen(),
    const ChatHistoryscreen(),
    const chatScreen(),
    const profilescreen(),
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
              color: Color.fromARGB(255, 47, 100, 253), // Background color

            ),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 10.0, vertical: 10.0),
              child: GNav(
                gap: 8,
                backgroundColor: Color.fromARGB(255, 47, 100, 253),
                activeColor: Colors.white,
                color: Colors.white,
                tabBackgroundColor: const Color.fromARGB(60, 255, 255, 255),
                padding: const EdgeInsets.all(10),
                tabs: const [
                  GButton(
                    icon: Icons.home_outlined,
                    text: 'Trang Chủ',
                    textSize: 20,
                  ),
                  GButton(
                    icon: Icons.history,
                    text: 'Lịch sử đặt lịch',
                    textSize: 20,
                  ),
                  GButton(
                    icon: Icons.chat,
                    text: 'Tin nhắn',
                    textSize: 20,
                  ),
                  GButton(
                    icon: Icons.person,
                    text: 'Tài khoản',
                    textSize: 20,
                  ),
                ],
                selectedIndex: chatProvider.currentIndex,
                onTabChange: (index) {
                  chatProvider.setCurrentIndex(newIndex: index);
                  chatProvider.pageController.jumpToPage(index);
                },
              ),
            ),
          ),
        );
      },
    );
  }
}
