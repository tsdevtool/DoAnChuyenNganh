package com.example.WebSiteDatLich.service;
import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.model.User;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.database.*;
import com.google.cloud.storage.Storage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
@Service
public class AdminService {
    private final FirebaseDatabase firebaseDatabase;
    private final Storage storage;
    public AdminService() {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.storage = StorageOptions.getDefaultInstance().getService();  // Dùng Google Cloud Storage
    }

    public CompletableFuture<List<Doctor>> getAllDoctors() {
        CompletableFuture<List<Doctor>> future = new CompletableFuture<>();
        DatabaseReference doctorsRef = firebaseDatabase.getReference("doctors");
        doctorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                List<Doctor> doctors = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        doctors.add(doctor);

                        // Fetch name and avatar from the User table using user_id
                        if (doctor.getUser_id() != null) {
                            DatabaseReference userRef = firebaseDatabase.getReference("users/" + doctor.getUser_id());
                            CompletableFuture<Void> userFuture = new CompletableFuture<>();
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot userSnapshot) {
                                    User user = userSnapshot.getValue(User.class);
                                    if (user != null) {
                                        // Fetch and display the user's name and avatar without storing in Doctor
                                        doctor.setUserName(user.getName());
                                        doctor.setAvatarUrl(user.getAvatar());
                                    }
                                    userFuture.complete(null);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    userFuture.completeExceptionally(new RuntimeException("Failed to load user"));
                                }
                            });
                            futures.add(userFuture);
                        }
                    }
                }
                // Wait for all user fetches to complete
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                    future.complete(doctors);
                }).exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Database error: " + databaseError.getMessage()));
            }
        });

        return future;
    }
    // Lấy thông tin chi tiết một bác sĩ theo ID
    public CompletableFuture<Doctor> getDoctorById(Long doctorId) {
        CompletableFuture<Doctor> future = new CompletableFuture<>();
        DatabaseReference doctorRef = firebaseDatabase.getReference("doctors").child(doctorId.toString());

        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Doctor doctor = dataSnapshot.getValue(Doctor.class);
                if (doctor != null && doctor.getUser_id() != null) {
                    // Truy xuất thông tin từ bảng users dựa trên user_id của bác sĩ
                    DatabaseReference userRef = firebaseDatabase.getReference("users").child(doctor.getUser_id());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                // Gán thông tin từ bảng User
                                doctor.setUserName(user.getName());
                                doctor.setAvatarUrl(user.getAvatar());
                            }
                            future.complete(doctor);  // Hoàn tất dữ liệu bác sĩ sau khi có thông tin user
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            future.completeExceptionally(new RuntimeException("Failed to load user information"));
                        }
                    });
                } else {
                    future.complete(doctor);  // Hoàn tất nếu không có user_id
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Database error: " + databaseError.getMessage()));
            }
        });

        return future;
    }


}
