package com.example.WebSiteDatLich.service;

import com.example.WebSiteDatLich.model.Appointment;
import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.model.Work_schedule;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.WebSiteDatLich.service.EmailService;


@Service
public class AppointmentService {

    private final FirebaseDatabase firebaseDatabase;

    private final EmailService emailService;

    public AppointmentService(EmailService emailService) {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.emailService = emailService;
    }
///////////////////////////////////HÀM TRUY XUẤT DATA TỪ FIREBASE/////////////////////////////////////////////////////
public CompletableFuture<List<Appointment>> getUnconfirmedAppointmentsWithDetails() {
    CompletableFuture<List<Appointment>> future = new CompletableFuture<>();
    DatabaseReference appointmentRef = firebaseDatabase.getReference("appointments");

    appointmentRef.orderByChild("status").equalTo(2)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<Appointment> unconfirmedAppointments = new ArrayList<>();
                    for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                        Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                        if (appointment != null) {
                            // Thiết lập appointment_id từ key của snapshot
                            appointment.setAppointment_id(appointmentSnapshot.getKey());
                            unconfirmedAppointments.add(appointment);
                        }
                    }
                    future.complete(unconfirmedAppointments);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    future.completeExceptionally(new RuntimeException("Failed to load appointments"));
                }
            });

    return future;
}

    public CompletableFuture<List<Appointment>> getConfirmedAppointmentsWithDetails(int status) {
        CompletableFuture<List<Appointment>> future = new CompletableFuture<>();
        DatabaseReference appointmentRef = firebaseDatabase.getReference("appointments");

        appointmentRef.orderByChild("status").equalTo(status)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Appointment> confirmedAppointments = new ArrayList<>();
                        for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                            Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                            if (appointment != null) {
                                confirmedAppointments.add(appointment);
                            }
                        }
                        future.complete(confirmedAppointments);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException("Failed to load confirmed appointments"));
                    }
                });

        return future;
    }

    ///////////////////////////////////HÀM TRUY XUẤT DATA TỪ FIREBASE////////////////////////////////////////////////////////////

//////////////////////////////////////////NEW/////////////////////////////////////////////////////////////////////////////////
public CompletableFuture<Void> confirmAppointment(String appointmentId) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    DatabaseReference appointmentRef = firebaseDatabase.getReference("appointments").child(appointmentId);

    appointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            // Debug: kiểm tra snapshot dữ liệu từ Firebase
            System.out.println("Snapshot data: " + snapshot.getValue());

            Appointment appointment = snapshot.getValue(Appointment.class);
            if (appointment != null) {
                // Thiết lập appointment_id từ key của snapshot
                appointment.setAppointment_id(snapshot.getKey());

                // Debug: kiểm tra thông tin cuộc hẹn
                System.out.println("Appointment ID found: " + appointment.getAppointment_id());
                System.out.println("User ID from appointment: " + appointment.getUser_id());

                // Đảm bảo rằng user_id không bị null hoặc rỗng
                if (appointment.getUser_id() == null || appointment.getUser_id().isEmpty()) {
                    System.out.println("User ID is null or empty in appointment data");
                    future.completeExceptionally(new RuntimeException("User ID not found in appointment data"));
                    return;
                }

                // Lấy thông tin User
                DatabaseReference userRef = firebaseDatabase.getReference("user").child(appointment.getUser_id());
                System.out.println("Fetching user data for user ID: " + appointment.getUser_id());

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot userSnapshot) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            // Thiết lập user_id từ key của snapshot
                            user.setUser_id(userSnapshot.getKey());
                            System.out.println("User found: " + user.getUser_id());
                            System.out.println("User email: " + user.getEmail());

                            // Kiểm tra email người dùng
                            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                                // Tiếp tục xử lý
                                String toEmail = user.getEmail();
                                String fullName = appointment.getPatientName();
                                String phoneNumber = appointment.getPhoneNumber();
                                String address = user.getAddress();
                                Boolean sexBoolean = user.getSex();
                                String sex = sexBoolean != null && sexBoolean ? "Nam" : "Nữ";

                                String subject = "Xác nhận cuộc hẹn";
                                String body = String.format(
                                        "Xin chào <b>%s</b>,<br><br>Cuộc hẹn của bạn đã được xác nhận với các thông tin chi tiết:<br>"
                                                + "<b>Giới tính:</b> %s<br>"
                                                + "<b>Ngày:</b> %s<br>"
                                                + "<b>Giờ:</b> %s<br>"
                                                + "<b>Số điện thoại:</b> %s<br>"
                                                + "<b>Địa chỉ:</b> %s<br><br>"
                                                + "Cảm ơn quý khách đã tin tưởng khám bệnh tại phòng khám của chúng tôi!",
                                        fullName,
                                        sex,
                                        appointment.getAppointmentDate(),
                                        appointment.getAppointmentTime(),
                                        phoneNumber,
                                        address
                                );

                                // Cập nhật trạng thái của cuộc hẹn và gửi email
                                appointmentRef.child("status").setValue(1, (databaseError, databaseReference) -> {
                                    if (databaseError == null) {
                                        emailService.sendConfirmationEmail(toEmail, subject, body);
                                        future.complete(null);
                                    } else {
                                        System.out.println("Failed to update status of appointment: " + appointmentId);
                                        future.completeExceptionally(new RuntimeException("Failed to confirm appointment"));
                                    }
                                });
                            } else {
                                System.out.println("User email is null or empty for user ID: " + appointment.getUser_id());
                                future.completeExceptionally(new RuntimeException("User email not found"));
                            }
                        } else {
                            System.out.println("User not found with ID: " + appointment.getUser_id());
                            future.completeExceptionally(new RuntimeException("User not found in Firebase"));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println("Failed to fetch user data: " + error.getMessage());
                        future.completeExceptionally(new RuntimeException("Failed to fetch user data"));
                    }
                });
            } else {
                System.out.println("Appointment not found with ID: " + appointmentId);
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


///////////////////////////////////HÀM XÁC NHẬN LỊCH ĐẶT KHÁM////////////////////////////////////////////////////////////////


///////////////////////////////////HÀM HỦY LỊCH ĐẶT KHÁM/////////////////////////////////////////////////////////////////////
public CompletableFuture<Void> cancelAppointment(String appointmentId) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    DatabaseReference appointmentRef = firebaseDatabase.getReference("appointments").child(appointmentId);

    // Lấy đối tượng Appointment từ Firebase để kiểm tra xem nó có tồn tại không
    appointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            if (snapshot.exists()) {
                // Thay đổi trạng thái cuộc hẹn thành 4 (đã hủy)
                appointmentRef.child("status").setValue(4, (databaseError, databaseReference) -> {
                    if (databaseError == null) {
                        System.out.println("Appointment canceled successfully.");
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new RuntimeException("Failed to cancel appointment: " + databaseError.getMessage()));
                    }
                });
            } else {
                future.completeExceptionally(new RuntimeException("Appointment not found"));
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            future.completeExceptionally(new RuntimeException("Failed to fetch appointment"));
        }
    });

    return future;
}

///////////////////////////////////HÀM HỦY LỊCH ĐẶT KHÁM/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////
public CompletableFuture<Appointment> getAppointmentById(String appointmentId) {
    CompletableFuture<Appointment> future = new CompletableFuture<>();
    DatabaseReference appointmentRef = firebaseDatabase.getReference("appointments").child(appointmentId);

    appointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            Appointment appointment = snapshot.getValue(Appointment.class);
            if (appointment != null) {
                appointment.setAppointment_id(snapshot.getKey());
                future.complete(appointment);
            } else {
                future.complete(null);
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            future.completeExceptionally(new RuntimeException("Failed to load appointment with ID: " + appointmentId));
        }
    });

    return future;
}


}
