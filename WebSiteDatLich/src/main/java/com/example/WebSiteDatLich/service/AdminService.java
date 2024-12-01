package com.example.WebSiteDatLich.service;

import com.example.WebSiteDatLich.model.Diagnose;
import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.model.User;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.database.*;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class AdminService {
    private final FirebaseDatabase firebaseDatabase;
    private final Storage storage;
    public AdminService() {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.storage = StorageOptions.getDefaultInstance().getService();  // Dùng Google Cloud Storage
    }

    //Lấy thông tin bác sĩ
    public CompletableFuture<List<Doctor>> getAllDoctorsAsync() {
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
                        System.out.println("Doctor found: " + doctor.getDoctor_id()); // Log kiểm tra
                        doctors.add(doctor);

                        if (doctor.getUser_id() != null && !doctor.getUser_id().isEmpty()) {
                            DatabaseReference userRef = firebaseDatabase.getReference("user").child(doctor.getUser_id());
                            CompletableFuture<Void> userFuture = new CompletableFuture<>();

                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot userSnapshot) {
                                    User user = userSnapshot.getValue(User.class);
                                    if (user != null) {
                                        System.out.println("User found: " + user.getName()); // Log kiểm tra
                                        doctor.setUserName(user.getName());
                                        doctor.setAvatarUrl(user.getAvatar());
                                    }
                                    userFuture.complete(null);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    System.err.println("Failed to load user: " + databaseError.getMessage());
                                    userFuture.completeExceptionally(new RuntimeException("Failed to load user information"));
                                }
                            });

                            futures.add(userFuture);
                        }
                    }
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenRun(() -> {
                            System.out.println("Doctors data loaded successfully."); // Log kiểm tra
                            future.complete(doctors);
                        })
                        .exceptionally(ex -> {
                            future.completeExceptionally(ex);
                            return null;
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
                future.completeExceptionally(new RuntimeException("Database error: " + databaseError.getMessage()));
            }
        });

        return future;
    }



    //lấy tất cả dữ liệu khoa và chức vụ
    public CompletableFuture<List<Map<String, Object>>> getAllDepartments() {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
        DatabaseReference departmentsRef = firebaseDatabase.getReference("departments");

        departmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Map<String, Object>> departments = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> department = new HashMap<>();
                    // Kiểm tra và lấy các giá trị từ snapshot
                    department.put("id", snapshot.getKey()); // Lấy ID làm khóa
                    department.put("department_id", snapshot.child("department_id").getValue());
                    department.put("image", snapshot.child("image").getValue(String.class));
                    department.put("name", snapshot.child("name").getValue(String.class));
                    departments.add(department);
                }
                future.complete(departments);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Failed to load departments: " + databaseError.getMessage()));
            }
        });

        return future;
    }


    public CompletableFuture<Map<Integer, String>> getAllPositions() {
        CompletableFuture<Map<Integer, String>> future = new CompletableFuture<>();
        DatabaseReference positionsRef = firebaseDatabase.getReference("positions");

        positionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<Integer, String> positions = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    positions.put(Integer.parseInt(snapshot.getKey()), snapshot.getValue(String.class));
                }
                future.complete(positions);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Failed to load positions: " + databaseError.getMessage()));
            }
        });

        return future;
    }
    // Lấy thông tin tên khoa và chức vụ dựa trên id
    public CompletableFuture<Map<Integer, String>> getDepartmentNames() {
        CompletableFuture<Map<Integer, String>> future = new CompletableFuture<>();
        DatabaseReference departmentsRef = firebaseDatabase.getReference("departments");

        departmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<Integer, String> departmentNames = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Integer departmentId = snapshot.child("department_id").getValue(Integer.class);
                    String name = snapshot.child("name").getValue(String.class);
                    if (departmentId != null && name != null) {
                        departmentNames.put(departmentId, name);
                    }
                }
                future.complete(departmentNames);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Failed to load departments: " + databaseError.getMessage()));
            }
        });

        return future;
    }

    public CompletableFuture<Map<Integer, String>> getPositionNames() {
        CompletableFuture<Map<Integer, String>> future = new CompletableFuture<>();
        DatabaseReference positionsRef = firebaseDatabase.getReference("positions");

        positionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<Integer, String> positionNames = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Integer positionId = Integer.parseInt(snapshot.getKey());
                    String name = snapshot.getValue(String.class);
                    positionNames.put(positionId, name);
                }
                future.complete(positionNames);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Failed to load positions: " + databaseError.getMessage()));
            }
        });

        return future;
    }
    //Xóa thông tin bác sĩ
    public CompletableFuture<Boolean> deleteDoctor(Long doctorId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        DatabaseReference doctorRef = firebaseDatabase.getReference("doctors").child(String.valueOf(doctorId));

        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    doctorRef.removeValue((databaseError, databaseReference) -> {
                        if (databaseError == null) {
                            future.complete(true);
                        } else {
                            future.completeExceptionally(new RuntimeException(databaseError.getMessage()));
                        }
                    });
                } else {
                    future.completeExceptionally(new RuntimeException("Bác sĩ không tồn tại."));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Lỗi cơ sở dữ liệu: " + databaseError.getMessage()));
            }
        });

        return future;
    }
    // Thêm chuyên khoa mới với ID tự động
    public CompletableFuture<Boolean> addNewDepartment(String name, String imageUrl) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        DatabaseReference departmentsRef = firebaseDatabase.getReference("departments");

        // Tạo ID tự động cho chuyên khoa mới
        String departmentId = departmentsRef.push().getKey();

        Map<String, Object> newDepartment = new HashMap<>();
        newDepartment.put("department_id", departmentId);
        newDepartment.put("name", name);

        newDepartment.put("image", imageUrl);

        // Lưu thông tin vào Firebase Realtime Database
        departmentsRef.child(departmentId).setValue(newDepartment, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                future.complete(true);
            } else {
                future.completeExceptionally(new RuntimeException("Lỗi khi thêm chuyên khoa: " + databaseError.getMessage()));
            }
        });

        return future;
    }


    // Xóa thông tin chuyên khoa
    public CompletableFuture<Boolean> deleteDepartment(String departmentId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        DatabaseReference departmentRef = firebaseDatabase.getReference("departments").child(departmentId);

        departmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    departmentRef.removeValue((databaseError, databaseReference) -> {
                        if (databaseError == null) {
                            future.complete(true); // Xóa thành công
                        } else {
                            future.completeExceptionally(new RuntimeException("Không thể xóa chuyên khoa: " + databaseError.getMessage()));
                        }
                    });
                } else {
                    future.completeExceptionally(new RuntimeException("Chuyên khoa không tồn tại."));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Lỗi cơ sở dữ liệu: " + databaseError.getMessage()));
            }
        });

        return future;
    }
    //Cập nhập tên chuyên khoa
    public CompletableFuture<Boolean> updateDepartment(String departmentId, String name, String imageUrl) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        DatabaseReference departmentRef = firebaseDatabase.getReference("departments").child(departmentId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        if (imageUrl != null) {
            updates.put("image", imageUrl);
        }

        departmentRef.updateChildren(updates, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                future.complete(true);
            } else {
                future.completeExceptionally(new RuntimeException("Cập nhật thất bại: " + databaseError.getMessage()));
            }
        });

        return future;
    }


    public String uploadImageToFirebaseStorage(MultipartFile file, String departmentId) {
        String fileName = "departments/" + departmentId + "_" + file.getOriginalFilename();
        try {
            // Tạo tham chiếu đến Firebase Storage
            Bucket bucket = StorageClient.getInstance().bucket();
            Blob blob = bucket.create(fileName, file.getBytes(), file.getContentType());

            // Trả về đường dẫn tải trực tiếp của ảnh
            return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucket.getName(), URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()));
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tải ảnh lên Firebase Storage: " + e.getMessage(), e);
        }
    }
    public String uploadImageToFirebaseStorage(MultipartFile file) {
        String fileName = "departments/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            // Tạo tham chiếu đến Firebase Storage
            Bucket bucket = StorageClient.getInstance().bucket();
            Blob blob = bucket.create(fileName, file.getBytes(), file.getContentType());

            // Trả về đường dẫn tải trực tiếp của ảnh
            return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucket.getName(), URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()));
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tải ảnh lên Firebase Storage: " + e.getMessage(), e);
        }
    }

    //Quản lý danh sách người dùng User
    public CompletableFuture<List<Map<String, Object>>> getAllUsers() {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
        DatabaseReference usersRef = firebaseDatabase.getReference("user");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Map<String, Object>> users = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> user = (Map<String, Object>) snapshot.getValue();
                    user.put("user_id", snapshot.getKey());
                    users.add(user);
                }
                future.complete(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException(databaseError.getMessage()));
            }
        });

        return future;
    }
    /*public void updateUserRole(String userId, int roleId) {
        DatabaseReference userRef = firebaseDatabase.getReference("user").child(userId);

        // Dữ liệu cần cập nhật
        Map<String, Object> updates = new HashMap<>();
        updates.put("role_id", roleId);

        // Cập nhật quyền
        userRef.updateChildrenAsync(updates).addListener(() -> {
            System.out.println("Cập nhật role_id thành công cho userId: " + userId);
        }, error -> {
            throw new RuntimeException("Lỗi khi cập nhật role_id: " + error.getClass());
        });
    }*/

    public CompletableFuture<Void> updateUserRole(String userId, int newRoleId) {
        DatabaseReference userRef = firebaseDatabase.getReference("user").child(userId);
        DatabaseReference doctorsRef = firebaseDatabase.getReference("doctors");

        CompletableFuture<Void> future = new CompletableFuture<>();

        // Lấy thông tin user trước khi cập nhật
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer currentRoleId = snapshot.child("role_id").getValue(Integer.class);

                    if (currentRoleId != null && currentRoleId == 2 && newRoleId != 2) {
                        // Nếu role_id ban đầu là 2 và role_id mới khác 2
                        // Tìm và xóa dữ liệu trong bảng doctors
                        doctorsRef.orderByChild("user_id").equalTo(userId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot doctorSnapshot) {
                                        for (DataSnapshot doctor : doctorSnapshot.getChildren()) {
                                            // Xóa dữ liệu bác sĩ với CompletionListener
                                            doctor.getRef().removeValue((error, ref) -> {
                                                if (error == null) {
                                                    System.out.println("Xóa dữ liệu bác sĩ thành công cho user_id: " + userId);
                                                } else {
                                                    System.err.println("Lỗi khi xóa dữ liệu bác sĩ: " + error.getMessage());
                                                }
                                            });
                                        }

                                        // Cập nhật role_id sau khi xóa dữ liệu
                                        updateRoleId(userRef, newRoleId, future);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        future.completeExceptionally(new RuntimeException("Lỗi khi tìm kiếm dữ liệu bác sĩ: " + error.getMessage()));
                                    }
                                });
                    } else {
                        // Nếu không cần xóa dữ liệu, cập nhật role_id trực tiếp
                        updateRoleId(userRef, newRoleId, future);
                    }
                } else {
                    future.completeExceptionally(new RuntimeException("Không tìm thấy user với ID: " + userId));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Lỗi khi lấy dữ liệu người dùng: " + error.getMessage()));
            }
        });

        return future;
    }

    // Phương thức riêng để cập nhật role_id
    public void updateRoleId(DatabaseReference userRef, int newRoleId, CompletableFuture<Void> future) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("role_id", newRoleId);

        userRef.updateChildrenAsync(updates).addListener(() -> {
            System.out.println("Cập nhật role_id thành công!");
            future.complete(null);
        }, error -> {
            System.err.println("Lỗi khi cập nhật role_id: " + error.getClass());
        });
    }


    public CompletableFuture<Void> addUserToFirebase(String name, String email, String phone, String address, int roleId, boolean sex, String avatarUrl, String dateOfBirth, String rawPassword) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference usersRef = firebaseDatabase.getReference("user");
        String userId = usersRef.push().getKey();

        if (userId == null) {
            future.completeExceptionally(new RuntimeException("Không thể tạo ID người dùng mới."));
            return future;
        }

        // Mã hóa mật khẩu
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("user_id", userId);
        newUser.put("name", name);
        newUser.put("email", email);
        newUser.put("phone", phone);
        newUser.put("address", address);
        newUser.put("role_id", roleId);
        newUser.put("sex", sex);
        newUser.put("avatar", avatarUrl);
        newUser.put("date_of_birth", dateOfBirth);
        newUser.put("password", hashedPassword); // Thêm mật khẩu đã mã hóa

        usersRef.child(userId).setValue(newUser, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                future.complete(null);
            } else {
                future.completeExceptionally(new RuntimeException("Lỗi khi thêm người dùng: " + databaseError.getMessage()));
            }
        });

        return future;
    }
    public CompletableFuture<List<User>> getUsersWithRoleDoctorNotInDoctors() {
        CompletableFuture<List<User>> future = new CompletableFuture<>();

        DatabaseReference usersRef = firebaseDatabase.getReference("user");
        DatabaseReference doctorsRef = firebaseDatabase.getReference("doctors");

        // Lấy tất cả danh sách doctor_id
        doctorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot doctorsSnapshot) {
                // Tạo danh sách ID của những user đã có thông tin bác sĩ
                Set<String> doctorUserIds = new HashSet<>();
                for (DataSnapshot doctorSnapshot : doctorsSnapshot.getChildren()) {
                    Doctor doctor = doctorSnapshot.getValue(Doctor.class);
                    if (doctor != null && doctor.getUser_id() != null) {
                        doctorUserIds.add(doctor.getUser_id());
                    }
                }

                // Lọc danh sách users dựa trên role_id = 2 và không nằm trong danh sách doctorUserIds
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot usersSnapshot) {
                        List<User> users = new ArrayList<>();
                        for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null && user.getRole_id() == 2 && !doctorUserIds.contains(userSnapshot.getKey())) {
                                user.setUser_id(userSnapshot.getKey());
                                users.add(user);
                            }
                        }
                        future.complete(users);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException("Lỗi khi lấy danh sách user: " + error.getMessage()));
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Lỗi khi lấy danh sách doctor: " + error.getMessage()));
            }
        });

        return future;
    }
    // Thêm bác sĩ mới với ID tự động
    public CompletableFuture<Boolean> addNewDoctor(String userId, String departmentId, int positionId, String education, String experience, String information) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        DatabaseReference doctorsRef = firebaseDatabase.getReference("doctors");

        String doctorId = doctorsRef.push().getKey();
        if (doctorId == null) {
            future.completeExceptionally(new RuntimeException("Không thể tạo ID mới cho bác sĩ."));
            return future;
        }

        Map<String, Object> newDoctor = new HashMap<>();
        newDoctor.put("doctor_id", doctorId);
        newDoctor.put("user_id", userId);
        newDoctor.put("department_id", departmentId); // Sử dụng String
        newDoctor.put("position_id", positionId);
        newDoctor.put("education", education);
        newDoctor.put("experience", experience);
        newDoctor.put("information", information);

        doctorsRef.child(doctorId).setValue(newDoctor, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                future.complete(true);
            } else {
                future.completeExceptionally(new RuntimeException("Lỗi khi thêm bác sĩ: " + databaseError.getMessage()));
            }
        });

        return future;
    }



    public CompletableFuture<Map<String, Object>> getDepartments() {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        DatabaseReference departmentsRef = firebaseDatabase.getReference("departments");

        departmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Object> departments = new HashMap<>();
                for (DataSnapshot departmentSnapshot : snapshot.getChildren()) {
                    departments.put(departmentSnapshot.getKey(), departmentSnapshot.getValue());
                }
                future.complete(departments);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Lỗi khi lấy danh sách departments: " + error.getMessage()));
            }
        });

        return future;
    }

    public CompletableFuture<Map<String, String>> getPositions() {
        CompletableFuture<Map<String, String>> future = new CompletableFuture<>();
        DatabaseReference positionsRef = firebaseDatabase.getReference("positions");

        positionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, String> positions = new HashMap<>();
                for (DataSnapshot positionSnapshot : snapshot.getChildren()) {
                    positions.put(positionSnapshot.getKey(), positionSnapshot.getValue(String.class));
                }
                future.complete(positions);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Lỗi khi lấy danh sách positions: " + error.getMessage()));
            }
        });

        return future;
    }

    //Cập nhập thông tin bác sĩ
    public CompletableFuture<Boolean> updateDoctor(String doctorId, String departmentId,
                                                   Integer positionId, String education,
                                                   String experience, String information) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        DatabaseReference doctorRef = firebaseDatabase.getReference("doctors").child(doctorId);

        Map<String, Object> updates = new HashMap<>();
        if (departmentId != null) {
            updates.put("department_id", departmentId);
        }
        if (positionId != null) {
            updates.put("position_id", positionId);
        }
        if (education != null) {
            updates.put("education", education);
        }
        if (experience != null) {
            updates.put("experience", experience);
        }
        if (information != null) {
            updates.put("information", information);
        }

        doctorRef.updateChildren(updates, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                future.complete(true);
            } else {
                future.completeExceptionally(
                        new RuntimeException("Cập nhật thất bại: " + databaseError.getMessage()));
            }
        });

        return future;
    }
    public CompletableFuture<Doctor> getDoctorById(String doctorId) {
        CompletableFuture<Doctor> future = new CompletableFuture<>();
        DatabaseReference doctorRef = firebaseDatabase.getReference("doctors").child(doctorId);

        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Doctor doctor = snapshot.getValue(Doctor.class);

                    // Lấy thông tin user dựa trên user_id
                    DatabaseReference userRef = firebaseDatabase.getReference("user").child(doctor.getUser_id());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                Map<String, Object> userData = (Map<String, Object>) userSnapshot.getValue();
                                doctor.setUserName((String) userData.get("name"));
                                doctor.setAvatarUrl((String) userData.get("avatar")); // Đường dẫn ảnh
                                future.complete(doctor);
                            } else {
                                future.completeExceptionally(new RuntimeException("User không tồn tại: " + doctor.getUser_id()));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            future.completeExceptionally(new RuntimeException("Lỗi truy vấn user: " + error.getMessage()));
                        }
                    });
                } else {
                    future.completeExceptionally(new RuntimeException("Doctor không tồn tại: " + doctorId));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Lỗi truy vấn doctor: " + error.getMessage()));
            }
        });

        return future;
    }
    public CompletableFuture<Map<String, Object>> getDoctorWithDetailsById(String doctorId) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        DatabaseReference doctorRef = firebaseDatabase.getReference("doctors").child(doctorId);

        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot doctorSnapshot) {
                if (doctorSnapshot.exists()) {
                    Doctor doctor = doctorSnapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("doctor", doctor);

                        CompletableFuture<Void> userFuture = new CompletableFuture<>();
                        CompletableFuture<Void> departmentFuture = new CompletableFuture<>();
                        CompletableFuture<Void> positionFuture = new CompletableFuture<>();

                        // Lấy thông tin user
                        DatabaseReference userRef = firebaseDatabase.getReference("user").child(doctor.getUser_id());
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot userSnapshot) {
                                if (userSnapshot.exists()) {
                                    result.put("userName", userSnapshot.child("name").getValue(String.class));
                                    result.put("avatarUrl", userSnapshot.child("avatar").getValue(String.class));
                                }
                                userFuture.complete(null);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                userFuture.completeExceptionally(new RuntimeException("Lỗi khi lấy thông tin user: " + error.getMessage()));
                            }
                        });

                        // Lấy tên chuyên khoa
                        DatabaseReference departmentRef = firebaseDatabase.getReference("departments").child(doctor.getDepartment_id());
                        departmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot departmentSnapshot) {
                                if (departmentSnapshot.exists()) {
                                    result.put("departmentName", departmentSnapshot.child("name").getValue(String.class));
                                }
                                departmentFuture.complete(null);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                departmentFuture.completeExceptionally(new RuntimeException("Lỗi khi lấy tên chuyên khoa: " + error.getMessage()));
                            }
                        });

                        // Lấy tên chức vụ
                        DatabaseReference positionRef = firebaseDatabase.getReference("positions").child(String.valueOf(doctor.getPosition_id()));
                        positionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot positionSnapshot) {
                                if (positionSnapshot.exists()) {
                                    result.put("positionName", positionSnapshot.getValue(String.class));
                                }
                                positionFuture.complete(null);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                positionFuture.completeExceptionally(new RuntimeException("Lỗi khi lấy tên chức vụ: " + error.getMessage()));
                            }
                        });

                        CompletableFuture.allOf(userFuture, departmentFuture, positionFuture)
                                .thenRun(() -> future.complete(result))
                                .exceptionally(ex -> {
                                    future.completeExceptionally(ex);
                                    return null;
                                });
                    } else {
                        future.completeExceptionally(new RuntimeException("Không tìm thấy thông tin bác sĩ."));
                    }
                } else {
                    future.completeExceptionally(new RuntimeException("Không tìm thấy bác sĩ với ID: " + doctorId));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Lỗi truy vấn bác sĩ: " + error.getMessage()));
            }
        });

        return future;
    }


    public CompletableFuture<Map<String, Object>> getUserWithDetailsById(String userId) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        DatabaseReference userRef = firebaseDatabase.getReference("user").child(userId);

        System.out.println("Đang truy vấn Firebase với userId: " + userId); // Log kiểm tra

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    System.out.println("Dữ liệu người dùng tồn tại: " + userSnapshot.getValue()); // Log dữ liệu

                    Map<String, Object> userDetails = new HashMap<>();
                    userDetails.put("user", userSnapshot.getValue(User.class)); // Map vào đối tượng User
                    future.complete(userDetails);
                } else {
                    System.err.println("Không tìm thấy người dùng với ID: " + userId); // Log lỗi
                    future.completeExceptionally(new RuntimeException("Không tìm thấy người dùng với ID: " + userId));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Lỗi truy vấn Firebase: " + error.getMessage()); // Log lỗi Firebase
                future.completeExceptionally(new RuntimeException("Lỗi khi truy vấn người dùng: " + error.getMessage()));
            }
        });

        return future;
    }
    public CompletableFuture<Void> addPositionWithNumericId(String name) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference positionsRef = firebaseDatabase.getReference("positions");

        positionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long maxId = 0;

                // Duyệt qua các ID hiện có để tìm ID lớn nhất
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        long currentId = Long.parseLong(snapshot.getKey());
                        if (currentId > maxId) {
                            maxId = currentId;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("ID không hợp lệ: " + snapshot.getKey());
                    }
                }

                // ID mới là maxId + 1
                long newId = maxId + 1;

                // Lưu chức vụ mới
                positionsRef.child(String.valueOf(newId)).setValue(name, (databaseError, databaseReference) -> {
                    if (databaseError == null) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(new RuntimeException("Lỗi khi thêm chức vụ: " + databaseError.getMessage()));
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Lỗi khi truy vấn dữ liệu: " + databaseError.getMessage()));
            }
        });

        return future;
    }


    public CompletableFuture<Void> updatePosition(String id, String name) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference positionRef = firebaseDatabase.getReference("positions").child(id);

        positionRef.setValue(name, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                future.complete(null);
            } else {
                future.completeExceptionally(new RuntimeException("Cập nhật chức vụ thất bại: " + databaseError.getMessage()));
            }
        });

        return future;
    }


    public CompletableFuture<Void> deletePosition(String id) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference positionRef = firebaseDatabase.getReference("positions").child(id);

        positionRef.removeValue((databaseError, databaseReference) -> {
            if (databaseError == null) {
                future.complete(null);
            } else {
                future.completeExceptionally(new RuntimeException("Xóa chức vụ thất bại: " + databaseError.getMessage()));
            }
        });

        return future;
    }

    //Hiển thị danh sách chuẩn đoán
    // Lấy danh sách Diagnoses
    public CompletableFuture<List<Diagnose>> getAllDiagnoses() {
        CompletableFuture<List<Diagnose>> future = new CompletableFuture<>();
        DatabaseReference diagnosesRef = firebaseDatabase.getReference("diagnoses");

        diagnosesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Diagnose> diagnoses = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Diagnose diagnose = child.getValue(Diagnose.class);
                    if (diagnose != null) {
                        diagnose.setDiagnose_id(child.getKey());
                        diagnoses.add(diagnose);
                    }
                }
                future.complete(diagnoses);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Failed to load diagnoses: " + error.getMessage()));
            }
        });
        return future;
    }

    // Thêm chẩn đoán mới
    public CompletableFuture<Void> addDiagnose(String name, String departmentId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference diagnosesRef = firebaseDatabase.getReference("diagnoses");

        // Tạo ID mới tự động
        String diagnoseId = diagnosesRef.push().getKey();
        if (diagnoseId == null) {
            future.completeExceptionally(new RuntimeException("Failed to generate diagnose ID."));
            return future;
        }

        Diagnose newDiagnose = new Diagnose();
        newDiagnose.setDiagnose_id(diagnoseId);
        newDiagnose.setName(name);
        newDiagnose.setDepartment_id(departmentId);

        diagnosesRef.child(diagnoseId).setValue(newDiagnose, (error, ref) -> {
            if (error == null) {
                future.complete(null);
            } else {
                future.completeExceptionally(new RuntimeException("Failed to add diagnose: " + error.getMessage()));
            }
        });

        return future;
    }
    // Lấy danh sách chẩn đoán cùng tên chuyên khoa
    public CompletableFuture<List<Map<String, Object>>> getAllDiagnosesWithDepartmentNames() {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
        DatabaseReference diagnosesRef = firebaseDatabase.getReference("diagnoses");
        DatabaseReference departmentsRef = firebaseDatabase.getReference("departments");

        diagnosesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot diagnosesSnapshot) {
                Map<String, String> departmentNames = new HashMap<>();
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                // Lấy tên tất cả chuyên khoa trước
                departmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot departmentsSnapshot) {
                        for (DataSnapshot department : departmentsSnapshot.getChildren()) {
                            String departmentId = department.getKey();
                            String departmentName = department.child("name").getValue(String.class);
                            departmentNames.put(departmentId, departmentName);
                        }

                        // Xử lý danh sách chẩn đoán
                        List<Map<String, Object>> diagnoses = new ArrayList<>();
                        for (DataSnapshot diagnoseSnapshot : diagnosesSnapshot.getChildren()) {
                            Map<String, Object> diagnose = new HashMap<>();
                            diagnose.put("diagnose_id", diagnoseSnapshot.getKey());
                            diagnose.put("name", diagnoseSnapshot.child("name").getValue(String.class));
                            String departmentId = diagnoseSnapshot.child("department_id").getValue(String.class);
                            diagnose.put("departmentName", departmentNames.get(departmentId));
                            diagnoses.add(diagnose);
                        }
                        future.complete(diagnoses);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException("Failed to load departments: " + error.getMessage()));
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Failed to load diagnoses: " + error.getMessage()));
            }
        });

        return future;
    }
    // Xóa chẩn đoán
    public CompletableFuture<Void> deleteDiagnose(String id) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference diagnosesRef = firebaseDatabase.getReference("diagnoses");

        diagnosesRef.child(id).removeValue((error, ref) -> {
            if (error == null) {
                future.complete(null);
            } else {
                future.completeExceptionally(new RuntimeException("Failed to delete diagnose: " + error.getMessage()));
            }
        });
        return future;
    }

    // Cập nhật chẩn đoán
    public CompletableFuture<Void> updateDiagnose(Diagnose diagnose) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference diagnosesRef = firebaseDatabase.getReference("diagnoses");

        diagnosesRef.child(diagnose.getDiagnose_id()).setValue(diagnose, (error, ref) -> {
            if (error == null) {
                future.complete(null);
            } else {
                future.completeExceptionally(new RuntimeException("Failed to update diagnose: " + error.getMessage()));
            }
        });
        return future;
    }

    // Lấy chẩn đoán theo ID
    public CompletableFuture<Diagnose> getDiagnoseById(String id) {
        CompletableFuture<Diagnose> future = new CompletableFuture<>();
        DatabaseReference diagnosesRef = firebaseDatabase.getReference("diagnoses");

        diagnosesRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Diagnose diagnose = snapshot.getValue(Diagnose.class);
                if (diagnose != null) {
                    diagnose.setDiagnose_id(id);
                    future.complete(diagnose);
                } else {
                    future.completeExceptionally(new RuntimeException("Diagnose not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Failed to get diagnose: " + error.getMessage()));
            }
        });
        return future;
    }

}

