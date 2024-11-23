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
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    public void login(String email, String password, LoginCallback callback) {
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                            callback.onSuccess("Login successful!");
                        } else {
                            callback.onFailure("Invalid email or password!");
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

        // Gán vai trò mặc định
        user.setRole_id(1);

        // Xử lý upload ảnh lên Firebase Storage (nếu có)
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                Bucket bucket = StorageClient.getInstance().bucket();
                String fileName = UUID.randomUUID().toString() + "_" + avatarFile.getOriginalFilename();
                Blob blob = bucket.create(fileName, avatarFile.getInputStream(), avatarFile.getContentType());
                // Tạo URL tải xuống công khai có thời hạn
                URL url = blob.signUrl(14, TimeUnit.DAYS); // URL có hiệu lực trong 14 ngày
                    user.setAvatar(url.toString());

            } catch (IOException e) {
                callback.onFailure("Failed to upload avatar: " + e.getMessage());
                return;
            }
        } else {
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
