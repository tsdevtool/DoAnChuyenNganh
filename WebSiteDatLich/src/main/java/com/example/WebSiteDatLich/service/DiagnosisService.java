package com.example.WebSiteDatLich.service;

import com.example.WebSiteDatLich.model.Appointment;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;
import com.google.api.core.ApiFuture;
@Service
public class DiagnosisService {

    private final FirebaseDatabase firebaseDatabase;

    public DiagnosisService() {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
    }

    /**
     * Lưu thông tin chuẩn đoán bệnh vào Firebase.
     *
     * @param appointmentId  ID của cuộc hẹn
     * @param diagnoseName   Tên chuẩn đoán
     * @param treatmentName  Phương pháp điều trị
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Void> saveDiagnosis(String appointmentId, String diagnoseName, String treatmentName) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference detailAppointmentRef = firebaseDatabase.getReference("detail_appointments").child(appointmentId);

        // Dữ liệu chuẩn đoán và appointment ID
        Map<String, Object> diagnosisData = new HashMap<>();
        diagnosisData.put("appointment_id", appointmentId); // Lưu thêm appointmentId
        diagnosisData.put("diagnose_name", diagnoseName);
        diagnosisData.put("treatment_name", treatmentName);

        // Gửi dữ liệu đến Firebase
        ApiFuture<Void> writeFuture = detailAppointmentRef.updateChildrenAsync(diagnosisData);

        // Xử lý kết quả
        new Thread(() -> {
            try {
                writeFuture.get(); // Chờ tác vụ hoàn thành
                System.out.println("Chẩn đoán bệnh đã được lưu thành công với appointment_id.");
                future.complete(null);
            } catch (Exception e) {
                System.err.println("Lỗi khi lưu chẩn đoán bệnh: " + e.getMessage());
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    /**
     * Lấy thông tin chi tiết chuẩn đoán.
     *
     * @param appointmentId ID của cuộc hẹn
     * @return CompletableFuture<Map<String, Object>>
     */
    public CompletableFuture<Map<String, Object>> getDiagnosis(String appointmentId) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        DatabaseReference detailAppointmentRef = firebaseDatabase.getReference("detail_appointments").child(appointmentId);

        detailAppointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> diagnosisData = (Map<String, Object>) snapshot.getValue();
                    future.complete(diagnosisData);
                } else {
                    future.completeExceptionally(new RuntimeException("Không tìm thấy thông tin chuẩn đoán."));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Lỗi Firebase: " + error.getMessage()));
            }
        });

        return future;
    }
    public CompletableFuture<Appointment> getAppointmentById(String appointmentId) {
        CompletableFuture<Appointment> future = new CompletableFuture<>();
        DatabaseReference appointmentsRef = firebaseDatabase.getReference("appointments").child(appointmentId);

        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Chuyển dữ liệu từ Firebase thành đối tượng Appointment
                    Appointment appointment = snapshot.getValue(Appointment.class);
                    if (appointment != null) {
                        // Gán appointment_id thủ công vào đối tượng Appointment
                        appointment.setAppointment_id(appointmentId);
                        future.complete(appointment);
                    } else {
                        future.completeExceptionally(new RuntimeException("Dữ liệu không hợp lệ cho cuộc hẹn với ID: " + appointmentId));
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
}
