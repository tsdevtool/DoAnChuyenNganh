import 'package:firebase_database/firebase_database.dart';
import 'package:ungdungdatlichkham/models/Appointment.dart';

class Appointmentservice {
  final DatabaseReference _db = FirebaseDatabase.instance.ref();

  Future<void> addAppointment(Appointment appointment) async {
    try {
      await _db.child('appointments').push().set(appointment.toMap());
    } catch (e) {
      print("Error adding appointment: $e");
    }
  }
  Stream<List<Appointment>> getAppointmentsByUserId(String userId) {
    try {
      final Stream<DatabaseEvent> stream = _db
          .child('appointments')
          .orderByChild('user_id')
          .equalTo(userId)
          .onValue;

      return stream.map((event) {
        if (event.snapshot.exists) {
          final List<Appointment> appointments = [];
          final Map<dynamic, dynamic> data =
          event.snapshot.value as Map<dynamic, dynamic>;
          data.forEach((key, value) {
            appointments.add(Appointment.fromMap(Map<String, dynamic>.from(value)));
          });
          return appointments;
        } else {
          return [];
        }
      });
    } catch (e) {
      print("Error fetching appointments: $e");
      return const Stream.empty();
    }
  }

}