package com.example.WebSiteDatLich.service;

import com.example.WebSiteDatLich.model.Doctor;
import com.google.firebase.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
public class DoctorService {

    private final DatabaseReference doctorReference;

    public DoctorService() {
        // Khởi tạo Firebase Database tham chiếu tới node "doctors"
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        this.doctorReference = firebaseDatabase.getReference("doctors");

    }

    // Lấy danh sách bác sĩ từ Firebase Realtime Database
    public List<Doctor> getDoctors() throws InterruptedException {
        List<Doctor> doctorList = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);  // Sử dụng để chờ dữ liệu bất đồng bộ

        doctorReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println("DataSnapshot exists, processing data...");

                if (snapshot.exists()) {
                    System.out.println("Snapshot exists, iterating through doctors...");

                    for (DataSnapshot doctorSnapshot : snapshot.getChildren()) {
                        System.out.println("Processing doctor with key: " + doctorSnapshot.getKey());
                        Doctor doctor = doctorSnapshot.getValue(Doctor.class);
                        if (doctor != null) {
                            doctorList.add(doctor);
                            System.out.println("Doctor added: " + doctor.getInformation());  // Thay `getName()` bằng phương thức lấy tên phù hợp
                        } else {
                            System.err.println("Doctor data is null for key: " + doctorSnapshot.getKey());
                        }
                    }
                } else {
                    System.out.println("No data available in snapshot.");
                }

                latch.countDown(); // Thả latch khi xử lý xong
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        latch.await();  // Chờ đến khi dữ liệu được tải xong
        return doctorList;
    }
}