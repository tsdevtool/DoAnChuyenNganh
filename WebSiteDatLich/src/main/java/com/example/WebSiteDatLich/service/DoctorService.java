package com.example.WebSiteDatLich.service;
import com.example.WebSiteDatLich.model.Appointment;
import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.model.Work_schedule;
import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
@Service
public class DoctorService {

    private final FirebaseDatabase firebaseDatabase;
    private final Storage storage;
    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);
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
    public CompletableFuture<Doctor> getDoctorById(String doctorId) {
        CompletableFuture<Doctor> future = new CompletableFuture<>();
        DatabaseReference doctorRef = firebaseDatabase.getReference("doctors/" + doctorId);

        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Doctor doctor = dataSnapshot.getValue(Doctor.class);
                if (doctor != null) {
                    logger.info("Tìm thấy thông tin bác sĩ: {}", doctor);

                    CompletableFuture<Void> userFuture = new CompletableFuture<>();
                    CompletableFuture<Void> workScheduleFuture = new CompletableFuture<>();
                    CompletableFuture<Void> departmentFuture = new CompletableFuture<>();
                    CompletableFuture<Void> positionFuture = new CompletableFuture<>();
                    // Lấy thông tin user
                    if (doctor.getUser_id() != null) {
                        DatabaseReference userRef = firebaseDatabase.getReference("users/" + doctor.getUser_id());
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot userSnapshot) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null) {
                                    doctor.setUserName(user.getName());
                                    doctor.setAvatarUrl(user.getAvatar());
                                }
                                logger.info("Lấy thông tin người dùng thành công: {}", user);
                                userFuture.complete(null);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                userFuture.completeExceptionally(new RuntimeException("Không thể tải thông tin người dùng: " + databaseError.getMessage()));
                            }
                        });
                    } else {
                        userFuture.complete(null);
                    }
                    // Lấy thông tin department
                    if (doctor.getDepartment_id() != null) {
                        logger.info("Đang truy xuất thông tin khoa với ID: {}", doctor.getDepartment_id());
                        DatabaseReference departmentRef = firebaseDatabase.getReference("departments/" + doctor.getDepartment_id());
                        departmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot departmentSnapshot) {
                                String departmentName = departmentSnapshot.getValue(String.class);
                                doctor.setDepartmentName(departmentName);
                                logger.info("Lấy thông tin khoa thành công: {}", departmentName);
                                departmentFuture.complete(null);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                logger.error("Không thể tải thông tin khoa: {}", databaseError.getMessage());
                                departmentFuture.completeExceptionally(new RuntimeException("Không thể tải thông tin khoa: " + databaseError.getMessage()));
                            }
                        });
                    } else {
                        departmentFuture.complete(null);
                    }
                    // Lấy thông tin position
                    if (doctor.getPosition_id() != null) {
                        logger.info("Đang truy xuất thông tin chức vụ với ID: {}", doctor.getPosition_id());
                        DatabaseReference positionRef = firebaseDatabase.getReference("positions/" + doctor.getPosition_id());
                        positionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot positionSnapshot) {
                                String positionName = positionSnapshot.getValue(String.class);
                                doctor.setPositionName(positionName);
                                logger.info("Lấy thông tin chức vụ thành công: {}", positionName);
                                positionFuture.complete(null);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                logger.error("Không thể tải thông tin chức vụ: {}", databaseError.getMessage());
                                positionFuture.completeExceptionally(new RuntimeException("Không thể tải thông tin chức vụ: " + databaseError.getMessage()));
                            }
                        });
                    } else {
                        positionFuture.complete(null);
                    }

                    // Lấy danh sách work schedules
                    DatabaseReference workScheduleRef = firebaseDatabase.getReference("work_schedules");
                    workScheduleRef.orderByChild("doctor_id").equalTo(doctor.getDoctor_id())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    List<Work_schedule> workSchedules = new ArrayList<>();
                                    Gson gson = new Gson(); // Dùng để chuyển đổi nếu cần

                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Work_schedule schedule = snapshot.getValue(Work_schedule.class);

                                        // Xử lý `timeSlots` nếu cần
                                        if (schedule != null) {
                                            Object timeSlotsRaw = snapshot.child("timeSlots").getValue();

                                            if (timeSlotsRaw instanceof List) {
                                                // Nếu timeSlots đã là List
                                                schedule.setTimeSlots((List<String>) timeSlotsRaw);
                                            } else if (timeSlotsRaw instanceof String) {
                                                // Nếu timeSlots là chuỗi JSON
                                                String timeSlotsJson = (String) timeSlotsRaw;
                                                List<String> timeSlots = gson.fromJson(timeSlotsJson, new TypeToken<List<String>>() {}.getType());
                                                schedule.setTimeSlots(timeSlots);
                                            }

                                            workSchedules.add(schedule);
                                        }
                                    }

                                    doctor.setWorkSchedules(workSchedules);
                                    logger.info("Lấy danh sách lịch làm việc thành công: {}", workSchedules);
                                    workScheduleFuture.complete(null);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    logger.error("Không thể tải danh sách lịch làm việc: {}", databaseError.getMessage());
                                    workScheduleFuture.completeExceptionally(new RuntimeException("Không thể tải danh sách lịch làm việc: " + databaseError.getMessage()));
                                }
                            });

                    CompletableFuture.allOf(userFuture, workScheduleFuture)
                            .thenRun(() -> future.complete(doctor))
                            .exceptionally(ex -> {
                                future.completeExceptionally(ex);
                                return null;
                            });
                } else {
                    future.complete(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Không thể tải thông tin bác sĩ: " + databaseError.getMessage()));
            }
        });

        return future;
    }
    public CompletableFuture<String> confirmBooking(Appointment appointment, MultipartFile medicalImage) {
        CompletableFuture<String> future = new CompletableFuture<>();

        // Xử lý lưu ảnh lên Firebase Storage nếu có
        if (medicalImage != null && !medicalImage.isEmpty()) {
            try {
                Bucket bucket = StorageClient.getInstance().bucket();
                String fileName = UUID.randomUUID().toString() + "_" + medicalImage.getOriginalFilename();
                Blob blob = bucket.create(fileName, medicalImage.getInputStream(), medicalImage.getContentType());

                // Tạo URL tải xuống công khai có thời hạn
                URL imageUrl = blob.signUrl(14, TimeUnit.DAYS); // URL có hiệu lực trong 14 ngày
                appointment.setImage_medical_records(imageUrl.toString());

            } catch (IOException e) {
                logger.error("Lỗi khi lưu ảnh vào Firebase Storage: {}", e.getMessage());
                future.completeExceptionally(new RuntimeException("Không thể lưu ảnh lên Firebase Storage: " + e.getMessage()));
                return future;
            }
        }

        // Tạo mới lịch hẹn trong Firebase Realtime Database
        DatabaseReference appointmentsRef = firebaseDatabase.getReference("appointments");
        String newAppointmentKey = appointmentsRef.push().getKey();
        if (newAppointmentKey == null) {
            future.completeExceptionally(new RuntimeException("Không thể tạo khóa mới cho appointment"));
        } else {
            appointmentsRef.child(newAppointmentKey).setValue(appointment, (databaseError, databaseReference) -> {
                if (databaseError != null) {
                    future.completeExceptionally(new RuntimeException("Lỗi khi lưu appointment: " + databaseError.getMessage()));
                } else {
                    future.complete("Đặt lịch thành công");
                }
            });
        }

        return future;
    }
}