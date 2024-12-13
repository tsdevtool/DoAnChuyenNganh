import 'package:flutter/material.dart';
import 'package:google_nav_bar/google_nav_bar.dart';
import 'package:provider/provider.dart';
import 'package:ungdungdatlichkham/Screen/HomeScreen.dart';
import 'package:ungdungdatlichkham/Screen/chat_historyscreen.dart';
import 'package:ungdungdatlichkham/Screen/chat_screen.dart';
import 'package:ungdungdatlichkham/Screen/profile_screen.dart';
import 'package:ungdungdatlichkham/providers/chat_provider.dart';

class Home_Screen extends StatefulWidget {
  final int selectedIndex;
  const Home_Screen({super.key, this.selectedIndex=0});// mặc định tab đầu tiên bằng 0

  @override
  State<Home_Screen> createState() => _Home_ScreenState();
}

class _Home_ScreenState extends State<Home_Screen> {
  int _selectionIndex = 0;
  // List of screens
  final List<Widget> _screens = [
    const HomeScreen(),
    const ChatHistoryscreen(),
    chatScreen(isInDashboard: true),
    const ProfileScreen(),
  ];
  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    _selectionIndex= widget.selectedIndex; // Thiết lập tab từ giá trị truyền vào
  }
  void _navigateToSelectedTab(int index){
    if(_selectionIndex != index){
      setState(() {
        _selectionIndex=index;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
        return Scaffold(
          body: _screens[_selectionIndex],//Hiển thị màn hình dựa trên các tab được chọn
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
                selectedIndex: _selectionIndex,
                onTabChange: (index){
                  _navigateToSelectedTab(index);
                },
              ),
            ),
          ),
        );
  }
}
