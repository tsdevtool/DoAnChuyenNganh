package com.example.WebSiteDatLich.service;

import com.example.WebSiteDatLich.model.Appointment;
import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.model.Work_schedule;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Service
public class AppointmentService {

    private final FirebaseDatabase firebaseDatabase;

    public AppointmentService() {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
    }

    public CompletableFuture<List<Appointment>> getUnconfirmedAppointmentsWithDetails() {
        CompletableFuture<List<Appointment>> future = new CompletableFuture<>();
        DatabaseReference appointmentRef = firebaseDatabase.getReference("appointments");

        appointmentRef.orderByChild("status").equalTo(false)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<CompletableFuture<Appointment>> appointmentFutures = new ArrayList<>();

                        for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                            Appointment appointment = appointmentSnapshot.getValue(Appointment.class);

                            if (appointment != null) {
                                // Tạo CompletableFuture cho mỗi appointment
                                CompletableFuture<Appointment> appointmentFuture = fetchDetailsForAppointment(appointment);
                                appointmentFutures.add(appointmentFuture);
                            }
                        }

                        // Khi tất cả các truy xuất hoàn thành, kết hợp tất cả các kết quả
                        CompletableFuture.allOf(appointmentFutures.toArray(new CompletableFuture[0]))
                                .thenApply(v -> {
                                    List<Appointment> unconfirmedAppointments = new ArrayList<>();
                                    appointmentFutures.forEach(fut -> {
                                        try {
                                            unconfirmedAppointments.add(fut.get());
                                        } catch (Exception e) {
                                            future.completeExceptionally(e);
                                        }
                                    });
                                    future.complete(unconfirmedAppointments);
                                    return null;
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException("Failed to load appointments"));
                    }
                });

        return future;
    }

    private CompletableFuture<Appointment> fetchDetailsForAppointment(Appointment appointment) {
        CompletableFuture<Appointment> future = new CompletableFuture<>();

        // Fetch user details
        DatabaseReference userRef = firebaseDatabase.getReference("users").child(appointment.getUser_id());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userSnapshot) {
                User user = userSnapshot.getValue(User.class);
                if (user != null) {
                    appointment.setPatientName(user.getName());
                    appointment.setPhoneNumber(user.getPhone());

                    // Fetch work schedule details
                    DatabaseReference scheduleRef = firebaseDatabase.getReference("work_schedules").child(appointment.getWork_schedule_id().toString());
                    scheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot scheduleSnapshot) {
                            Work_schedule schedule = scheduleSnapshot.getValue(Work_schedule.class);
                            if (schedule != null) {
                                appointment.setAppointmentDate(schedule.getSchedule());
                                appointment.setAppointmentTime(schedule.getTime());
                            }
                            future.complete(appointment);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            future.completeExceptionally(new RuntimeException("Failed to load schedule data"));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Failed to load user data"));
            }
        });

        return future;
    }

//    public CompletableFuture<Void> confirmAppointment(String appointmentId) {
//        CompletableFuture<Void> future = new CompletableFuture<>();
//        DatabaseReference appointmentRef = firebaseDatabase.getReference("appointments/" + appointmentId);
//        appointmentRef.child("status").setValue(true, (databaseError, databaseReference) -> {
//            if (databaseError == null) {
//                future.complete(null);
//            } else {
//                future.completeExceptionally(new RuntimeException("Failed to confirm appointment"));
//            }
//        });
//        return future;
//    }
public CompletableFuture<Void> confirmAppointment(String appointmentId) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    DatabaseReference appointmentRef = firebaseDatabase.getReference("appointments/" + appointmentId);

    // Tạo một listener để lắng nghe kết quả từ Firebase
    appointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            // Lấy đối tượng Appointment từ Firebase
            Appointment appointment = snapshot.getValue(Appointment.class);

            if (appointment != null) {
                // Kiểm tra xem appointment_id có tồn tại trong đối tượng Appointment không
                System.out.println("Confirming appointment with ID: " + appointment.getAppointment_id());

                // Cập nhật trạng thái của cuộc hẹn
                appointmentRef.child("status").setValue(true, (databaseError, databaseReference) -> {
                    if (databaseError == null) {
                        System.out.println("Appointment confirmed successfully.");
                        future.complete(null);
                    } else {
                        System.out.println("Failed to confirm appointment: " + databaseError.getMessage());
                        future.completeExceptionally(new RuntimeException("Failed to confirm appointment"));
                    }
                });
            } else {
                System.out.println("Appointment not found!");
                future.completeExceptionally(new RuntimeException("Appointment not found"));
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            System.out.println("Failed to fetch appointment: " + error.getMessage());
            future.completeExceptionally(new RuntimeException("Failed to fetch appointment"));
        }
    });

    return future;
}



}
