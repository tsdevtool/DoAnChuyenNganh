package com.example.WebSiteDatLich.service;

import com.example.WebSiteDatLich.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.springframework.security.crypto.password.PasswordEncoder; // Để mã hóa mật khẩu
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.api.core.ApiFuture;
import com.google.firebase.cloud.StorageClient;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class UserService {

    private final DatabaseReference databaseReference;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Inject PasswordEncoder để mã hóa mật khẩu

    public UserService() {
        // Tham chiếu đến bảng "user" trong Firebase Realtime Database
        this.databaseReference = FirebaseDatabase.getInstance().getReference("user");
    }

    // Xử lý đăng nhập
    public void login(String userName, String password, LoginCallback callback) {
        databaseReference.orderByChild("user_name").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                            callback.onSuccess("Login successful!");
                        } else {
                            callback.onFailure("Invalid username or password!");
                        }
                    }
                } else {
                    callback.onFailure("User not found!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure("Error: " + databaseError.getMessage());
            }
        });
    }
    public void register(User user, MultipartFile avatarFile, RegisterCallback callback) {
        // Tạo ID mới cho người dùng
        String userId = databaseReference.push().getKey();
        user.setUser_id(userId);

        // Mã hóa mật khẩu trước khi lưu trữ
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Gán vai trò mặc định (ví dụ: role_id = 1 cho người dùng bình thường)
        user.setRole_id(1);

        // Xử lý upload ảnh lên Firebase Storage
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                // Lấy bucket từ Firebase Storage
                Bucket bucket = StorageClient.getInstance().bucket();

                // Tạo tên file duy nhất
                String fileName = UUID.randomUUID().toString() + "_" + avatarFile.getOriginalFilename();

                // Upload file lên Firebase Storage
                Blob blob = bucket.create(fileName, avatarFile.getInputStream(), avatarFile.getContentType());

                // Lấy URL của file vừa upload
                String avatarUrl = blob.getMediaLink();

                // Lưu URL của ảnh đại diện vào Firebase
                user.setAvatar(avatarUrl);
            } catch (IOException e) {
                callback.onFailure("Failed to upload avatar: " + e.getMessage());
                return;
            }
        } else {
            // Nếu không chọn ảnh, gán giá trị mặc định cho avatar
            user.setAvatar("default_avatar.png");
        }

        // Lưu đối tượng User trực tiếp vào Firebase Realtime Database
        try {
            ApiFuture<Void> future = databaseReference.child(userId).setValueAsync(user);
            future.get();  // Chờ cho đến khi hoàn thành
            callback.onSuccess("User registered successfully!");
        } catch (Exception e) {
            callback.onFailure("Failed to register user: " + e.getMessage());
        }
    }

    public interface LoginCallback {
        void onSuccess(String message);
        void onFailure(String message);
    }

    public interface RegisterCallback {
        void onSuccess(String message);
        void onFailure(String message);
    }
}
