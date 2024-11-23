package com.example.WebSiteDatLich.service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DataTestImportService {
    private FirebaseDatabase firebaseDatabase;

    public void importTestData() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
        }

        DatabaseReference workSchedulesRef = firebaseDatabase.getReference("work_schedules");

        // Dữ liệu test
        Map<String, Object> testData = new HashMap<>();

        // Tạo dữ liệu mẫu cho 10 ngày khác nhau
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> schedule = new HashMap<>();
            schedule.put("doctor_id", 1); // ID bác sĩ luôn là 1
            schedule.put("schedule", "2024-11-" + (20 + i)); // Ngày từ 2024-11-21 đến 2024-11-30
            schedule.put("timeSlots", IntStream.range(0, 20)
                    .mapToObj(j -> String.format("%02d:%02d-%02d:%02d", 8 + j / 2, (j % 2) * 30, 8 + j / 2, ((j % 2) + 1) * 30))
                    .collect(Collectors.toList())); // 20 khung giờ từ 08:00-18:30

            testData.put(String.valueOf(i), schedule);
        }

        // Lưu dữ liệu vào Firebase
        workSchedulesRef.updateChildren(testData, (error, ref) -> {
            if (error != null) {
                System.out.println("Không thể import dữ liệu test: " + error.getMessage());
            } else {
                System.out.println("Dữ liệu test đã được import thành công.");
            }
        });
    }
}
