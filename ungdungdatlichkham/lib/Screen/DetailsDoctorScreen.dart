import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:ungdungdatlichkham/Screen/ConfirmationScreen.dart';
import 'package:ungdungdatlichkham/models/Appointment.dart';
import 'package:ungdungdatlichkham/models/Doctor.dart';
import 'package:ungdungdatlichkham/models/WorkSchedule.dart';
import 'package:ungdungdatlichkham/service/WorkScheduleService.dart';

class DoctorDetailsScreen extends StatefulWidget {
  final Doctor doctor;
  final List<WorkSchedule> schedules;
  final Map<String, dynamic> departments;


  const DoctorDetailsScreen({
    super.key,
    required this.doctor,
    required this.schedules,
    this.departments = const {}, // Gán giá trị mặc định nếu không truyền
  });

  @override
  _DoctorDetailsScreenState createState() => _DoctorDetailsScreenState();
}

class _DoctorDetailsScreenState extends State<DoctorDetailsScreen> {
  String? selectedDate;
  String? selectedTimeSlot;
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    _fetchSchedules();
    // Mặc định chọn ngày hiện tại
    selectedDate = DateFormat('yyyy-MM-dd').format(DateTime.now());
  }

  Future<String?> _getUserId() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('userId');
  }

  Future<String?> _getPhone() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('phone');
  }

  Future<String?> _getUserName() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('userName');
  }

  String? _getWorkScheduleId(String? selectedDate) {
    if (selectedDate == null) return null;

    final schedule = widget.schedules.firstWhere(
          (schedule) => schedule.schedule == selectedDate,
      orElse: () => WorkSchedule(),
    );

    return schedule.workScheduleId?.toString();
  }

  void _fetchSchedules() async {
    WorkScheduleService service = WorkScheduleService();
    String doctorId = widget.doctor.doctorId ?? "";

    if (doctorId.isEmpty) {
      setState(() {
        isLoading = false;
      });
      return;
    }

    try {
      List<WorkSchedule> schedules =
      await service.getSchedulesByDoctorId(doctorId);

      setState(() {
        widget.schedules.clear();
        widget.schedules.addAll(schedules);
        isLoading = false;
      });
    } catch (e) {
      setState(() {
        isLoading = false;
      });
    }
  }



  @override
  Widget build(BuildContext context) {
    String getDepartmentName(String? departmentId) {
      if (departmentId == null || widget.departments.isEmpty) return "Không rõ chuyên khoa";
      return widget.departments[departmentId]?['name'] ?? "Không rõ chuyên khoa";
    }
    // Sắp xếp danh sách lịch trình theo ngày tháng năm
    final sortedSchedules = widget.schedules.toList()
      ..sort((a, b) {
        final dateA = DateTime.parse(a.schedule ?? ""); // Chuyển đổi ngày từ String
        final dateB = DateTime.parse(b.schedule ?? "");
        return dateA.compareTo(dateB); // Sắp xếp theo thứ tự ngày tăng dần
      });

    // Ngày hiện tại
    final today = DateTime.now();

    // Loại bỏ ngày trùng lặp dựa trên `schedule`
    final uniqueSchedules = sortedSchedules
        .map((e) => e.schedule)
        .toSet()
        .map((uniqueSchedule) => sortedSchedules.firstWhere((item) => item.schedule == uniqueSchedule))
        .toList();

    return Scaffold(
      appBar: AppBar(
        title: Text('Thông tin bác sĩ',
        style: TextStyle(fontSize: 22, color: Colors.white, fontWeight: FontWeight.w600,),),
        centerTitle: true,
        backgroundColor: Color.fromARGB(255, 47, 100, 253),
        leading: IconButton(
          icon: const Icon(
            Icons.arrow_back_ios, // iOS-style back icon
            color: Colors.white,
          ),
          onPressed: () {
            Navigator.pop(context); // Navigate back to the previous screen
          },
        ),
      ),
      backgroundColor: Colors.white,
      body: Stack(
        children: [
          Column(
            children: [
              Expanded(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.all(20.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Thông tin bác sĩ
                      Row(
                        children: [
                          CircleAvatar(
                            radius: 60,
                            backgroundImage: NetworkImage(
                                widget.doctor.avatarUrl ?? ""),
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                    Text(
                                      widget.doctor.userName ?? "Không rõ",
                                      style: const TextStyle(
                                          fontSize: 25,
                                        fontWeight: FontWeight.w600,


                                          //fontWeight: FontWeight.bold
                                      ),
                                      overflow: TextOverflow.visible, // Cho phép hiển thị nội dung vượt quá mà không bị cắt
                                      softWrap: true, // Hỗ trợ xuống dòng nếu nội dung quá dài

                                    ),
                                SizedBox(height: 5,),
                                Row(
                                  children: [
                                    Icon(
                                      Icons.verified,
                                      size: 20,
                                      color: Color.fromARGB(255, 47, 100, 253),
                                    ),
                                    SizedBox(width: 5,),
                                    Text('Bác sĩ',
                                    style:TextStyle(
                                      color: Color.fromARGB(255, 47, 100, 253),
                                      fontSize: 18,
                                      fontWeight: FontWeight.w600,
                                    ),)
                                  ],
                                ),
                                const SizedBox(height: 5),
                                Text(
                                  getDepartmentName(widget.doctor.departmentId),
                                  style: const TextStyle(fontSize: 19, fontWeight: FontWeight.w600,),
                                  overflow: TextOverflow.visible, // Cho phép nội dung hiển thị đầy đủ
                                  softWrap: true,
                                ),

                                const SizedBox(height: 4),

                              ],
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 20),
                      const Text(
                        "Đặt khám nhanh",
                        style: TextStyle(
                            fontSize: 20, fontWeight: FontWeight.w600,),
                      ),
                      const SizedBox(height: 10),
                      if (widget.schedules.isNotEmpty)
                        SizedBox(
                          height: 50,
                          child: ListView.builder(
                            scrollDirection: Axis.horizontal,
                            itemCount:  uniqueSchedules.length,
                            itemBuilder: (context, index) {
                              final schedule = uniqueSchedules[index];
                              final scheduleDate = DateTime.parse(schedule.schedule ?? "");
                              final isToday = scheduleDate.year == today.year &&
                                  scheduleDate.month == today.month &&
                                  scheduleDate.day == today.day;
                              final isPastDate = scheduleDate.isBefore(today) && !isToday;
                              return GestureDetector(
                                onTap: () {
                                  if (!isPastDate) {
                                    setState(() {
                                      selectedDate = schedule.schedule;
                                    });
                                  }
                                },
                                child: Container(
                                  margin: const EdgeInsets.symmetric(
                                      horizontal: 6),
                                  padding: const EdgeInsets.symmetric(
                                      horizontal: 16, vertical: 10),
                                  decoration: BoxDecoration(
                                    color: selectedDate == schedule.schedule
                                        ? Color.fromARGB(255, 47, 100, 253)
                                        : isPastDate
                                        ? Colors.grey.shade300
                                        : Colors.white,
                                    borderRadius: BorderRadius.circular(10),
                                    border: Border.all(
                                      color: isPastDate ? Colors.grey.shade300 : Color.fromARGB(255, 47, 100, 253),
                                      width: 1.5,
                                    ),
                                  ),
                                  child: Center(
                                    child: Text(
                                      DateFormat('dd/MM/yyyy').format(scheduleDate),
                                      style: TextStyle(
                                        color: isPastDate ? Colors.grey : Colors.black,
                                        fontWeight: isPastDate ? FontWeight.normal : FontWeight.bold,
                                        fontSize: 16,
                                      ),
                                    ),
                                  ),
                                ),
                              );
                            },
                          ),
                        ),
                      const SizedBox(height: 20),
                      if (selectedDate != null)
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              "Chọn giờ khám",
                              style: TextStyle(
                                fontSize: 20,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            const SizedBox(height: 10),

                            // Hiển thị Buổi sáng
                            const Row(
                              children: [
                                Icon(Icons.wb_sunny_outlined, color: Colors.orange),
                                SizedBox(width: 5),
                                Text(
                                  "Buổi sáng",
                                  style: TextStyle(
                                    fontSize: 18,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 5),
                            SingleChildScrollView(
                              scrollDirection: Axis.horizontal,
                              child: Row(
                                children: widget.schedules
                                    .firstWhere((schedule) => schedule.schedule == selectedDate)
                                    .timeSlots!
                                    .where((slot) {
                                  final time = DateFormat('HH:mm').parse(slot.split('-')[0]);
                                  return time.hour < 12; // Buổi sáng trước 12h
                                })
                                    .map((slot) {
                                  final isSelected = selectedTimeSlot == slot;
                                  return Padding(
                                    padding: const EdgeInsets.symmetric(horizontal: 6),
                                    child: GestureDetector(
                                      onTap: () {
                                        setState(() {
                                          selectedTimeSlot = slot;
                                        });
                                      },
                                      child: AnimatedContainer(
                                        duration: const Duration(milliseconds: 300),
                                        padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 20),
                                        decoration: BoxDecoration(
                                          color: isSelected ? Color.fromARGB(255, 47, 100, 253) : Colors.white,
                                          borderRadius: BorderRadius.circular(30),
                                          border: Border.all(
                                            color: isSelected ? Color.fromARGB(255, 47, 100, 253) : Colors.grey,
                                            width: 1.5,
                                          ),
                                          boxShadow: isSelected
                                              ? [
                                            BoxShadow(
                                              color: Colors.blue.withOpacity(0.3),
                                              blurRadius: 10,
                                              offset: const Offset(0, 4),
                                            ),
                                          ]
                                              : [],
                                        ),
                                        child: Row(
                                          children: [
                                            Icon(
                                              Icons.access_time,
                                              size: 18,
                                              color: isSelected ? Colors.white : Colors.black54,
                                            ),
                                            const SizedBox(width: 8),
                                            Text(
                                              slot,
                                              style: TextStyle(
                                                fontSize: 16,
                                                fontWeight: FontWeight.bold,
                                                color: isSelected ? Colors.white : Colors.black,
                                              ),
                                            ),
                                          ],
                                        ),
                                      ),
                                    ),
                                  );
                                })
                                    .toList(),
                              ),
                            ),

                            const SizedBox(height: 10),

                            // Hiển thị Buổi chiều
                            const Row(
                              children: [
                                Icon(Icons.cloud_outlined, color: Colors.blue),
                                SizedBox(width: 5),
                                Text(
                                  "Buổi chiều",
                                  style: TextStyle(
                                    fontSize: 18,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 5),
                            SingleChildScrollView(
                              scrollDirection: Axis.horizontal,
                              child: Row(
                                children: widget.schedules
                                    .firstWhere((schedule) => schedule.schedule == selectedDate)
                                    .timeSlots!
                                    .where((slot) {
                                  final time = DateFormat('HH:mm').parse(slot.split('-')[0]);
                                  return time.hour >= 12; // Buổi chiều từ 12h trở đi
                                })
                                    .map((slot) {
                                  final isSelected = selectedTimeSlot == slot; // Kiểm tra khung giờ được chọn
                                  return Padding(
                                    padding: const EdgeInsets.symmetric(horizontal: 6),
                                    child: GestureDetector(
                                      onTap: () {
                                        setState(() {
                                          selectedTimeSlot = slot; // Cập nhật khung giờ đã chọn
                                        });
                                      },
                                      child: AnimatedContainer(
                                        duration: const Duration(milliseconds: 300), // Hiệu ứng chuyển đổi
                                        padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 20),
                                        decoration: BoxDecoration(
                                          color: isSelected ? Color.fromARGB(255, 47, 100, 253) : Colors.white,
                                          borderRadius: BorderRadius.circular(30),
                                          border: Border.all(
                                            color: isSelected ? Color.fromARGB(255, 47, 100, 253) : Colors.grey,
                                            width: 1.5,
                                          ),
                                          boxShadow: isSelected
                                              ? [
                                            BoxShadow(
                                              color: Colors.blue.withOpacity(0.3),
                                              blurRadius: 10,
                                              offset: const Offset(0, 4),
                                            ),
                                          ]
                                              : [],
                                        ),
                                        child: Row(
                                          children: [
                                            Icon(
                                              Icons.access_time,
                                              size: 18,
                                              color: isSelected ? Colors.white : Colors.black54,
                                            ),
                                            const SizedBox(width: 8),
                                            Text(
                                              slot,
                                              style: TextStyle(
                                                fontSize: 16,
                                                fontWeight: FontWeight.bold,
                                                color: isSelected ? Colors.white : Colors.black,
                                              ),
                                            ),
                                          ],
                                        ),
                                      ),
                                    ),
                                  );
                                })
                                    .toList(),
                              ),
                            )
                            ,
                          ],
                        ),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const SizedBox(height: 20),
                          Row(
                            children: const [
                              Icon(Icons.info_outline, color: Colors.blue, size: 26), // Thêm icon trước tiêu đề
                              SizedBox(width: 8),
                              Text(
                                "Giới thiệu",
                                style: TextStyle(
                                  fontSize: 20,
                                  fontWeight: FontWeight.w600,
                                  color: Colors.blue, // Nổi bật với màu sắc
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 10),
                          Text(
                            widget.doctor.information ?? "Không có thông tin",
                            style: const TextStyle(fontSize: 18, color: Colors.black), // Màu chữ nhẹ nhàng hơn
                          ),
                          const SizedBox(height: 20),
                          Row(
                            children: const [
                              Icon(Icons.work_outline, color: Colors.green, size: 26), // Thêm icon trước tiêu đề
                              SizedBox(width: 8),
                              Text(
                                "Kinh nghiệm",
                                style: TextStyle(
                                  fontSize: 20,
                                  fontWeight: FontWeight.w600,
                                  color: Colors.green, // Nổi bật với màu sắc
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 10),
                          Text(
                            widget.doctor.experience ?? "Đang cập nhập",
                            style: const TextStyle(fontSize: 18, color: Colors.black),
                          ),
                          const SizedBox(height: 20),
                          Row(
                            children: const [
                              Icon(Icons.school_outlined, color: Colors.orange, size: 26), // Thêm icon trước tiêu đề
                              SizedBox(width: 8),
                              Text(
                                "Học vấn",
                                style: TextStyle(
                                  fontSize: 20,
                                  fontWeight: FontWeight.w600,
                                  color: Colors.orange, // Nổi bật với màu sắc
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 10),
                          Text(
                            widget.doctor.education ?? "Đang cập nhập",
                            style: const TextStyle(fontSize: 18, color: Colors.black),
                          ),
                        ],
                      )

                    ],
                  ),
                ),
              ),
              _buildFixedConfirmButton(),
            ],
          ),
          if (isLoading)
            const Center(
              child: CircularProgressIndicator(),
            ),
        ],
      ),
    );
  }

  Widget _buildFixedConfirmButton() {
    return Container(
      padding: const EdgeInsets.all(16.0),
      child: SizedBox(
        width: double.infinity,
        child: GestureDetector(
          onTap: () async {
            if (selectedDate != null && selectedTimeSlot != null) {
              String? userId = await _getUserId();
              String? phone = await _getPhone();
              String? workScheduleId = _getWorkScheduleId(selectedDate);
              String? patientName = await _getUserName();
              if (userId == null ||
                  phone == null ||
                  workScheduleId == null ||
                  patientName == null) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text("Vui lòng kiểm tra thông tin!")),
                );
                return;
              }
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => ConfirmationScreen(
                    appointment: Appointment(
                      patientName: patientName,
                      appointmentDate: selectedDate,
                      appointmentTime: selectedTimeSlot,
                      medicalCondition: "Không rõ",
                    ),
                    userId: userId,
                    workScheduleId: workScheduleId,
                    phoneNumber: phone,
                    doctorName: widget.doctor.userName ?? "",
                  ),
                ),
              );
            } else {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text("Chọn ngày và giờ!")),
              );
            }
          },
          child: Container(
            padding: const EdgeInsets.symmetric(vertical: 16.0),
            decoration: BoxDecoration(
              color: Color.fromARGB(255, 47, 100, 253),
              borderRadius: BorderRadius.circular(30),
              boxShadow: [
                BoxShadow(
                  color: Colors.blue.withOpacity(0.3),
                  blurRadius: 10,
                  offset: const Offset(0, 5), // Shadow below the button
                ),
              ],
            ),
            child: const Center(
              child: Text(
                "Đặt khám",
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 1.2,
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

}


