import 'package:flutter/material.dart';
import 'package:ungdungdatlichkham/models/Department.dart';
import 'package:ungdungdatlichkham/service/DepartmentService.dart';

class FilterScreen extends StatefulWidget {
  final String? selectedDepartmentId;

  const FilterScreen({Key? key, this.selectedDepartmentId}) : super(key: key);

  @override
  _FilterScreenState createState() => _FilterScreenState();
}

class _FilterScreenState extends State<FilterScreen> {
  final DepartmentService _departmentService = DepartmentService();
  List<Department> _departments = [];
  String? _selectedDepartmentId;

  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _selectedDepartmentId = widget.selectedDepartmentId;
    _fetchDepartments();
  }

  Future<void> _fetchDepartments() async {
    final departments = await _departmentService.getAllDepartments();
    setState(() {
      _departments = departments;
      _isLoading = false;
    });
  }

  void _resetFilters() {
    setState(() {
      _selectedDepartmentId = null; // Xóa lựa chọn bộ lọc
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Bộ lọc',
        style: TextStyle(
          fontSize: 23,
          color: Colors.white,
          fontWeight: FontWeight.bold,
          fontFamily: 'Roboto'
        ),),
        centerTitle: true,
        leading: IconButton(
          icon: Icon(Icons.arrow_back_ios_new,size: 25, color: Colors.white,),
          onPressed: (){
            Navigator.pop(context);
          },
        ),
        backgroundColor: Color.fromARGB(255, 47, 100, 253),

      ),
      backgroundColor: Colors.white,
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : Column(
        children: [
          Expanded(
            child: ListView.builder(
              itemCount: _departments.length,
              itemBuilder: (context, index) {
                final department = _departments[index];
                return RadioListTile<String>(
                  title: Text(department.name ?? 'Không rõ chuyên khoa', style: TextStyle(fontSize: 17, fontWeight: FontWeight.w500),),
                  value: department.departmentId!,
                  groupValue: _selectedDepartmentId,
                  activeColor: const Color.fromARGB(255, 47, 100, 253), // Màu xanh nước
                  controlAffinity: ListTileControlAffinity.trailing, // Đặt RadioButton bên phải
                  onChanged: (value) {
                    setState(() {
                      _selectedDepartmentId = value;
                    });
                  },
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 20.0),
            child: Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () {
                      _resetFilters();
                      Navigator.pop(context, null); // Trả về null khi xóa bộ lọc
                    },
                    style: OutlinedButton.styleFrom(
                      side: const BorderSide(
                        color: Colors.grey, // Màu viền nút
                      ),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10), // Bo góc lớn hơn
                      ),
                      backgroundColor: Colors.grey.shade200, // Màu nền xám nhạt
                      minimumSize: const Size(double.infinity, 50), // Chiều cao nút
                      padding: const EdgeInsets.symmetric(vertical: 15), // Padding bên trong nút
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: const [
                        Icon(Icons.refresh, color: Colors.grey, size: 22), // Biểu tượng
                        SizedBox(width: 8), // Khoảng cách giữa biểu tượng và text
                        Text(
                          "Xóa bộ lọc",
                          style: TextStyle(
                            fontSize: 18, // Font chữ lớn hơn
                            fontWeight: FontWeight.w500,
                            color: Colors.grey,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(width: 16), // Khoảng cách giữa 2 nút
                Expanded(
                  child: ElevatedButton(
                    onPressed: () {
                      Navigator.pop(context, _selectedDepartmentId);
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color.fromARGB(255, 47, 100, 253), // Màu xanh
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10), // Bo góc lớn hơn
                      ),
                      minimumSize: const Size(double.infinity, 50), // Chiều cao nút
                      padding: const EdgeInsets.symmetric(vertical: 15), // Padding bên trong nút
                    ),
                    child: const Text(
                      "Áp dụng",
                      style: TextStyle(
                        fontSize: 18, // Font chữ lớn hơn
                        fontWeight: FontWeight.bold,
                        color: Colors.white, // Màu chữ trắng
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),

        ],
      ),
    );
  }
}
