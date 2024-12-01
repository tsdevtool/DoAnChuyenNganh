package com.example.WebSiteDatLich.service;

import com.example.WebSiteDatLich.model.Work_schedule;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class WorkScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(WorkScheduleService.class);
    private final FirebaseDatabase firebaseDatabase;

    public WorkScheduleService() {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
    }

    public CompletableFuture<Void> saveWorkSchedule(Work_schedule workSchedule) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            DatabaseReference workSchedulesRef = firebaseDatabase.getReference("work_schedules");
            String key = workSchedulesRef.push().getKey(); // Tạo một ID duy nhất cho work_schedule
            if (key != null) {
                workSchedule.setWork_schedule_id(String.valueOf(key.hashCode()));
                workSchedulesRef.child(key).setValue(workSchedule, (error, ref) -> {
                    if (error != null) {
                        logger.error("Không thể lưu Work_schedule: {}", error.getMessage());
                        future.completeExceptionally(new RuntimeException("Không thể lưu Work_schedule: " + error.getMessage()));
                    } else {
                        logger.info("Lưu Work_schedule thành công: {}", workSchedule);
                        future.complete(null);
                    }
                });
            } else {
                throw new RuntimeException("Không thể tạo ID cho Work_schedule");
            }
        } catch (Exception ex) {
            logger.error("Có lỗi xảy ra khi lưu Work_schedule: {}", ex.getMessage());
            future.completeExceptionally(ex);
        }

        return future;
    }
}
