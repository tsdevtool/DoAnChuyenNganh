import 'package:flutter/material.dart';
import 'package:ungdungdatlichkham/models/Appointment.dart';
import 'package:ungdungdatlichkham/service/AppointmentService.dart';

class HistoryApointmentScreen extends StatefulWidget {
  final String userId;

  const HistoryApointmentScreen({Key? key, required this.userId}) : super(key: key);

  @override
  _HistoryApointmentScreen createState() => _HistoryApointmentScreen();
}

class _HistoryApointmentScreen extends State<HistoryApointmentScreen> {
  final Appointmentservice _appointmentService = Appointmentservice();
  late Future<List<Appointment>> _appointments;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Lịch sử đặt lịch',
        style: TextStyle(
          fontSize: 22,
          fontWeight: FontWeight.w500,
          color: Colors.white
        ),),
        centerTitle: true,
        backgroundColor: const Color.fromARGB(255, 47, 100, 253),
        leading: IconButton(
          icon: Icon(Icons.arrow_back_ios_new),
          iconSize: 25,
          color: Colors.white,
          onPressed: ()=> Navigator.pop(context),

        ),
      ),
      backgroundColor: Colors.white,
      body:StreamBuilder<List<Appointment>>(
        stream: _appointmentService.getAppointmentsByUserId(widget.userId),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Center(
              child: Text(
                'Lỗi khi tải dữ liệu: ${snapshot.error}',
                style: const TextStyle(color: Colors.red),
              ),
            );
          } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
            return const Center(child: Text('Không có lịch sử đặt lịch.'));
          }

          final appointments = snapshot.data!;
          return ListView.builder(
            itemCount: appointments.length,
            padding: const EdgeInsets.all(10),
            itemBuilder: (context, index) {
              final appointment = appointments[index];
              return _buildAppointmentCard(appointment);
            },
          );
        },
      ),
    );
  }

  Widget _buildAppointmentCard(Appointment appointment) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 8.0, horizontal: 8.0),
      elevation: 4,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(15),
      ),
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  Icons.person_outline,
                  color: Color.fromARGB(255, 47, 100, 253),
                  size: 30,
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    appointment.patientName ?? 'Không rõ',
                    style: const TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: Colors.black,
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: appointment.status == 2
                        ? Colors.amberAccent
                        : appointment.status == 1
                        ? Colors.green
                        : appointment.status == 3
                        ? Colors.blue
                        : Colors.red,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    appointment.status == 1
                        ? 'Đã xác nhận'
                        : appointment.status == 2
                        ? 'Đang chờ xác nhận'
                        : appointment.status == 3
                        ? 'Đã khám'
                        : 'Đã hủy',
                    style: const TextStyle(
                      fontSize: 16,
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            Divider(color: Colors.grey, thickness: 0.5,),
            const SizedBox(height: 12),
            Row(
              children: [
                Icon(
                  Icons.calendar_today,
                  color: Color.fromARGB(255, 47, 100, 253),
                  size: 25,
                ),
                const SizedBox(width: 8),
                Text(
                  appointment.appointmentDate ?? 'N/A',
                  style: const TextStyle(fontSize: 17, color: Colors.black),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Icon(
                  Icons.access_time,
                  color: Colors.orange,
                  size: 25,
                ),
                const SizedBox(width: 8),
                Text(
                  appointment.appointmentTime ?? 'N/A',
                  style: const TextStyle(fontSize: 17, color: Colors.black),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Icon(
                  Icons.local_hospital_outlined,
                  color: Colors.red,
                  size: 25,
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    appointment.medicalCondition ?? 'Không rõ bệnh lý',
                    style: const TextStyle(fontSize: 17, color: Colors.black),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
