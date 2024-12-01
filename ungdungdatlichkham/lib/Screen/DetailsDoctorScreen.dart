import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:ungdungdatlichkham/Screen/ConfirmationScreen.dart';
import 'package:ungdungdatlichkham/models/Appointment.dart';
import 'package:ungdungdatlichkham/models/Doctor.dart';
import 'package:ungdungdatlichkham/models/WorkSchedule.dart';
import 'package:ungdungdatlichkham/service/WorkScheduleService.dart';

class DoctorDetailsScreen extends StatefulWidget {
  final Doctor doctor;
  final List<WorkSchedule> schedules;

  const DoctorDetailsScreen({
    super.key,
    required this.doctor,
    required this.schedules,
  });

  @override
  _DoctorDetailsScreenState createState() => _DoctorDetailsScreenState();
}

class _DoctorDetailsScreenState extends State<DoctorDetailsScreen> {
  String? selectedMonth;
  String? selectedDate;
  String? selectedTimeSlot;
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    _fetchSchedules();
  }

  // Hàm lấy userId từ SharedPreferences
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

  // Hàm lấy workScheduleId dựa trên selectedDate
  String? _getWorkScheduleId(String? selectedDate) {
    if (selectedDate == null) return null;

    final schedule = widget.schedules.firstWhere(
          (schedule) => schedule.schedule == selectedDate,
      orElse: () => WorkSchedule(),
    );

    if (schedule.workScheduleId == null) {
      print("Cảnh báo: workScheduleId là null với lịch: $selectedDate");
    }

    return schedule.workScheduleId
        ?.toString(); // Chuyển đổi sang String nếu là int
  }

  // Hàm fetch lịch làm việc từ Firebase
  void _fetchSchedules() async {
    WorkScheduleService service = WorkScheduleService();
    String doctorId = widget.doctor.doctorId ?? "";

    if (doctorId.isEmpty) {
      print("Doctor ID không tồn tại");
      setState(() {
        isLoading = false;
      });
      return;
    }

    try {
      List<WorkSchedule> schedules = await service.getSchedulesByDoctorId(
          doctorId);

      setState(() {
        widget.schedules.clear();
        widget.schedules.addAll(schedules);
        isLoading = false;
      });

      print("Lịch làm việc đã tải thành công: ${schedules.length}");
    } catch (e) {
      print("Lỗi khi tải lịch làm việc: $e");
      setState(() {
        isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Thông tin bác sĩ", style: TextStyle(fontSize: 22)),
        backgroundColor: Colors.lightBlue,
        actions: [
          IconButton(
            icon: const Icon(
                Icons.favorite_border, size: 28, color: Colors.white),
            onPressed: () {
              // Xử lý lưu bác sĩ vào danh sách yêu thích
            },
          ),
          IconButton(
            icon: const Icon(Icons.help_outline, size: 28, color: Colors.white),
            onPressed: () {
              // Xử lý mở trang hỗ trợ
            },
          ),
        ],
      ),
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
                      // Hiển thị thông tin bác sĩ
                      Row(
                        children: [
                          CircleAvatar(
                            radius: 50,
                            backgroundImage: NetworkImage(widget.doctor
                                .avatarUrl ?? ""),
                            onBackgroundImageError: (_, __) =>
                            const Icon(Icons.person, size: 80),
                          ),
                          const SizedBox(width: 16),
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                widget.doctor.userName ?? "Không rõ",
                                style: const TextStyle(
                                    fontSize: 20, fontWeight: FontWeight.bold),
                              ),
                              const Text(
                                  "Bác sĩ", style: TextStyle(fontSize: 16)),
                              Text(
                                "${widget.doctor.experience ??
                                    "0"} năm kinh nghiệm",
                                style: const TextStyle(fontSize: 16),
                              ),
                              Text(widget.doctor.departmentId ?? "Không rõ",
                                  style: const TextStyle(fontSize: 16)),
                            ],
                          ),
                        ],
                      ),
                      const SizedBox(height: 20),
                      const Text(
                        "Lịch khám",
                        style: TextStyle(
                            fontSize: 20, fontWeight: FontWeight.bold),
                      ),
                      const SizedBox(height: 10),
                      if (widget.schedules.isNotEmpty)
                        SizedBox(
                          height: 80,
                          child: ListView.builder(
                            scrollDirection: Axis.horizontal,
                            itemCount: widget.schedules.length,
                            itemBuilder: (context, index) {
                              final schedule = widget.schedules[index];
                              DateTime dateTime =
                              DateTime.parse(schedule.schedule ?? "");
                              String day = dateTime.day.toString();

                              return GestureDetector(
                                onTap: () {
                                  setState(() {
                                    selectedDate = schedule.schedule;
                                  });
                                },
                                child: Container(
                                  width: 60,
                                  margin: const EdgeInsets.symmetric(
                                      horizontal: 6),
                                  decoration: BoxDecoration(
                                    color: selectedDate == schedule.schedule
                                        ? Colors.blue
                                        : Colors.grey,
                                    border: Border.all(color: Colors.black),
                                    borderRadius: BorderRadius.circular(32),
                                  ),
                                  child: Center(
                                    child: Text(
                                      day,
                                      style: const TextStyle(
                                          color: Colors.black,
                                          fontWeight: FontWeight.bold,
                                          fontSize: 20),
                                    ),
                                  ),
                                ),
                              );
                            },
                          ),
                        ),
                      if (selectedDate != null)
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              "Buổi chiều",
                              style: TextStyle(
                                  fontSize: 18, fontWeight: FontWeight.bold),
                            ),
                            const SizedBox(height: 10),
                            SingleChildScrollView(
                              scrollDirection: Axis.horizontal,
                              child: Row(
                                children: widget.schedules
                                    .firstWhere(
                                        (schedule) =>
                                    schedule.schedule == selectedDate)
                                    .timeSlots!
                                    .map((slot) {
                                  return Container(
                                    margin: const EdgeInsets.symmetric(
                                        horizontal: 6),
                                    decoration: BoxDecoration(
                                      color: Colors.white,
                                      border: Border.all(color: Colors.black),
                                      borderRadius: BorderRadius.circular(10),
                                    ),
                                    child: ElevatedButton(
                                      onPressed: () {
                                        setState(() {
                                          selectedTimeSlot = slot;
                                        });
                                        print("Đã chọn giờ: $slot");
                                      },
                                      style: ElevatedButton.styleFrom(
                                        backgroundColor: selectedTimeSlot ==
                                            slot
                                            ? Colors.blue
                                            : Colors.white,
                                        foregroundColor: selectedTimeSlot ==
                                            slot
                                            ? Colors.white
                                            : Colors.black,
                                        padding: const EdgeInsets.symmetric(
                                            vertical: 10, horizontal: 16),
                                      ),
                                      child: Text(
                                        slot,
                                        style: const TextStyle(
                                            fontSize: 18,
                                            fontWeight: FontWeight.bold),
                                      ),
                                    ),
                                  );
                                }).toList(),
                              ),
                            ),
                          ],
                        ),
                    ],
                  ),
                ),
              ),
              _buildFixedConfirmButton(), // Nút Đặt khám cố định
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
      color: Colors.white, // Nền trắng để phân tách rõ ràng
      padding: const EdgeInsets.all(16.0),
      child: SizedBox(
        width: double.infinity,
        child: ElevatedButton(
          onPressed: () async {
            if (selectedDate != null && selectedTimeSlot != null) {
              String? userId = await _getUserId();
              String? phone = await _getPhone();
              String? workScheduleId = _getWorkScheduleId(selectedDate);
              String? patientName = await _getUserName();
              String? doctorName = widget.doctor.userName;
              if (userId == null ||
                  workScheduleId == null ||
                  phone == null ||
                  doctorName == null) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                      content: Text(
                          "Thông tin không đầy đủ. Vui lòng thử lại.")),
                );
                return;
              }

              Appointment appointment = Appointment(
                patientName: patientName,
                appointmentDate: selectedDate,
                appointmentTime: selectedTimeSlot,
                medicalCondition: "Tình trạng sức khỏe",
              );

              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) =>
                      ConfirmationScreen(
                        appointment: appointment,
                        userId: userId,
                        workScheduleId: workScheduleId,
                        phoneNumber: phone,
                        doctorName: doctorName,
                      ),
                ),
              );
            } else {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                    content:
                    Text("Vui lòng chọn ngày và giờ trước khi đặt lịch.")),
              );
            }
          },
          style: ElevatedButton.styleFrom(
            padding: const EdgeInsets.symmetric(vertical: 16),
            backgroundColor: Colors.lightBlue,
          ),
          child: const Text(
            "Đặt khám",
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white),
          ),
        ),
      ),
    );
  }
}
