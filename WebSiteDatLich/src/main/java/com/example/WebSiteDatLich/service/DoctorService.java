package com.example.WebSiteDatLich.service;

import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.model.User;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;
import com.google.cloud.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class DoctorService {
    private final FirebaseDatabase firebaseDatabase;
    private final Storage storage;
    public DoctorService() {
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

}