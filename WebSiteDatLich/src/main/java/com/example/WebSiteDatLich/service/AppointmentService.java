package com.example.WebSiteDatLich.service;

import com.example.WebSiteDatLich.model.Appointment;
import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.model.Detail_Appointment;
import com.example.WebSiteDatLich.model.Work_schedule;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                            System.out.println("Key from Firebase: " + appointmentSnapshot.getKey()); // Debug
                            Appointment appointment = appointmentSnapshot.getValue(Appointment.class);
                            if (appointment != null) {
                                appointment.setAppointment_id(appointmentSnapshot.getKey()); // Gán key
                                confirmedAppointments.add(appointment);
                            } else {
                                System.out.println("Null data at key: " + appointmentSnapshot.getKey());
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
                                        emailService.sendEmail(toEmail, subject, body);
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
public CompletableFuture<Appointment> getAppointmentById(String appointmentId) {
    CompletableFuture<Appointment> future = new CompletableFuture<>();
    DatabaseReference appointmentsRef = firebaseDatabase.getReference("appointments").child(appointmentId);

    // Lấy thông tin cuộc hẹn
    appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            if (snapshot.exists()) {
                Appointment appointment = snapshot.getValue(Appointment.class);
                if (appointment != null) {
                    // Gán appointment_id từ key của snapshot
                    appointment.setAppointment_id(snapshot.getKey());

                    // Lấy thông tin chi tiết từ Firebase
                    CompletableFuture<User> userFuture = getUserDetails(appointment.getUser_id());
                    CompletableFuture<String> doctorNameFuture = getDoctorName(appointment.getWork_schedule_id());
                    CompletableFuture<Work_schedule> workScheduleFuture = getWorkScheduleDetails(appointment.getWork_schedule_id());
                    CompletableFuture<Detail_Appointment> detailAppointmentFuture = getDetailAppointment(appointment.getAppointment_id());

                    // Đợi tất cả dữ liệu được tải xong
                    CompletableFuture.allOf(userFuture, doctorNameFuture, workScheduleFuture, detailAppointmentFuture)
                            .thenAccept(v -> {
                                try {
                                    // Lấy kết quả từ các CompletableFuture
                                    User user = userFuture.get();
                                    String doctorName = doctorNameFuture.get();
                                    Work_schedule workSchedule = workScheduleFuture.get();
                                    Detail_Appointment detailAppointment = detailAppointmentFuture.get();

                                    // Thiết lập thông tin vào đối tượng Appointment
                                    appointment.setPatientName(user.getName());
                                    appointment.setPhoneNumber(user.getPhone());
                                    appointment.setPatientEmail(user.getEmail());
                                    appointment.setPatientAddress(user.getAddress());
                                    Boolean sexBoolean = user.getSex();
                                    String sex = (sexBoolean != null && sexBoolean) ? "Nam" : "Nữ";
                                    appointment.setPatientSex(sex);
                                    appointment.setDoctorName(doctorName);
                                    appointment.setAppointmentDate(workSchedule.getSchedule());
//                                    appointment.setAppointmentTime(workSchedule.getTimeSlots());
                                    appointment.setDiagnoseName(detailAppointment.getDiagnose_name());
                                    appointment.setTreatmentName(detailAppointment.getTreatment_name());

                                    future.complete(appointment);
                                } catch (Exception e) {
                                    future.completeExceptionally(new RuntimeException("Lỗi khi lấy dữ liệu chi tiết: " + e.getMessage()));
                                }
                            });
                } else {
                    future.completeExceptionally(new RuntimeException("Không thể đọc dữ liệu cuộc hẹn"));
                }
            } else {
                future.completeExceptionally(new RuntimeException("Không tìm thấy cuộc hẹn với ID: " + appointmentId));
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            future.completeExceptionally(new RuntimeException("Lỗi Firebase: " + error.getMessage()));
        }
    });

    return future;
}

    private CompletableFuture<User> getUserDetails(String userId) {
        CompletableFuture<User> future = new CompletableFuture<>();
        DatabaseReference usersRef = firebaseDatabase.getReference("users").child(userId);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        user.setUser_id(snapshot.getKey()); // Gán key từ snapshot
                        future.complete(user);
                    } else {
                        future.completeExceptionally(new RuntimeException("Dữ liệu người dùng không hợp lệ"));
                    }
                } else {
                    future.completeExceptionally(new RuntimeException("Không tìm thấy người dùng với ID: " + userId));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Lỗi Firebase: " + error.getMessage()));
            }
        });

        return future;
    }

    private CompletableFuture<String> getDoctorName(String workScheduleId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        DatabaseReference workSchedulesRef = firebaseDatabase.getReference("work_schedules").child(workScheduleId);

        workSchedulesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Work_schedule workSchedule = snapshot.getValue(Work_schedule.class);
                    if (workSchedule != null) {
                        workSchedule.setWork_schedule_id(snapshot.getKey()); // Gán key từ snapshot
                        String doctorId = workSchedule.getDoctor_id();

                        // Lấy thông tin bác sĩ
                        DatabaseReference doctorsRef = firebaseDatabase.getReference("doctors").child(doctorId);
                        doctorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot doctorSnapshot) {
                                if (doctorSnapshot.exists()) {
                                    Doctor doctor = doctorSnapshot.getValue(Doctor.class);
                                    if (doctor != null) {
                                        doctor.setDoctor_id(doctorSnapshot.getKey()); // Gán key từ snapshot
                                        future.complete(doctor.getUserName());
                                    } else {
                                        future.completeExceptionally(new RuntimeException("Dữ liệu bác sĩ không hợp lệ"));
                                    }
                                } else {
                                    future.completeExceptionally(new RuntimeException("Không tìm thấy bác sĩ với ID: " + doctorId));
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                future.completeExceptionally(new RuntimeException("Lỗi Firebase: " + error.getMessage()));
                            }
                        });
                    } else {
                        future.completeExceptionally(new RuntimeException("Dữ liệu lịch làm việc không hợp lệ"));
                    }
                } else {
                    future.completeExceptionally(new RuntimeException("Không tìm thấy lịch làm việc với ID: " + workScheduleId));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Lỗi Firebase: " + error.getMessage()));
            }
        });

        return future;
    }

    private CompletableFuture<Detail_Appointment> getDetailAppointment(String appointmentId) {
        CompletableFuture<Detail_Appointment> future = new CompletableFuture<>();
        DatabaseReference detailAppointmentRef = firebaseDatabase.getReference("detail_appointments").child(appointmentId);

        detailAppointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Detail_Appointment detailAppointment = snapshot.getValue(Detail_Appointment.class);
                    if (detailAppointment != null) {
                        detailAppointment.setDetail_appointment_id(snapshot.getKey()); // Gán key từ snapshot
                        future.complete(detailAppointment);
                    } else {
                        future.completeExceptionally(new RuntimeException("Dữ liệu chi tiết cuộc hẹn không hợp lệ"));
                    }
                } else {
                    future.completeExceptionally(new RuntimeException("Không tìm thấy chi tiết cuộc hẹn với ID: " + appointmentId));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Lỗi Firebase: " + error.getMessage()));
            }
        });

        return future;
    }


    private CompletableFuture<Work_schedule> getWorkScheduleDetails(String workScheduleId) {
        CompletableFuture<Work_schedule> future = new CompletableFuture<>();
        DatabaseReference workSchedulesRef = firebaseDatabase.getReference("work_schedules").child(workScheduleId);

        workSchedulesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Work_schedule workSchedule = snapshot.getValue(Work_schedule.class);
                    if (workSchedule != null) {
                        workSchedule.setWork_schedule_id(snapshot.getKey()); // Gán key từ snapshot
                        future.complete(workSchedule);
                    } else {
                        future.completeExceptionally(new RuntimeException("Dữ liệu lịch làm việc không hợp lệ"));
                    }
                } else {
                    future.completeExceptionally(new RuntimeException("Không tìm thấy lịch làm việc với ID: " + workScheduleId));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Lỗi Firebase: " + error.getMessage()));
            }
        });

        return future;
    }

    public CompletableFuture<Void> updateDetailAppointment(String appointmentId, String diagnoseName, String treatmentName) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference detailAppointmentRef = firebaseDatabase.getReference("detail_appointments").child(appointmentId);

        Detail_Appointment detailAppointment = new Detail_Appointment();
        detailAppointment.setDiagnose_name(diagnoseName);
        detailAppointment.setTreatment_name(treatmentName);

        detailAppointmentRef.setValue(detailAppointment, (error, ref) -> {
            if (error == null) {
                future.complete(null);
            } else {
                future.completeExceptionally(new RuntimeException("Lỗi khi cập nhật chi tiết cuộc hẹn: " + error.getMessage()));
            }
        });

        return future;
    }


}

