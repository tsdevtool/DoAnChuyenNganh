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
}