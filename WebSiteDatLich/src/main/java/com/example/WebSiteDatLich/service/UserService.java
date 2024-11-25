package com.example.WebSiteDatLich.service;

import com.example.WebSiteDatLich.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private PasswordEncoder passwordEncoder;

    public UserService() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference("user");
    }

    // Đăng nhập
    public void login(String email, String password, LoginCallback callback) {
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();
                    User user = userSnapshot.getValue(User.class);

                    if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("Sai mật khẩu hoặc tài khoản!");
                    }
                } else {
                    callback.onFailure("Email không tồn tại!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure("Lỗi kết nối cơ sở dữ liệu: " + databaseError.getMessage());
            }
        });
    }

    // Đăng ký
    public void register(User user, MultipartFile avatarFile, RegisterCallback callback) {
        String userId = databaseReference.push().getKey();
        user.setUser_id(userId);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole_id(1);

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                Bucket bucket = StorageClient.getInstance().bucket();
                String fileName = UUID.randomUUID().toString() + "_" + avatarFile.getOriginalFilename();
                Blob blob = bucket.create(fileName, avatarFile.getInputStream(), avatarFile.getContentType());
                URL url = blob.signUrl(14, TimeUnit.DAYS);
                user.setAvatar(url.toString());
            } catch (IOException e) {
                callback.onFailure("Failed to upload avatar: " + e.getMessage());
                return;
            }
        } else {
            user.setAvatar("default_avatar.png");
        }

        try {
            ApiFuture<Void> future = databaseReference.child(userId).setValueAsync(user);
            future.get();
            callback.onSuccess("User registered successfully!");
        } catch (Exception e) {
            callback.onFailure("Failed to register user: " + e.getMessage());
        }
    }

    public interface LoginCallback {
        void onSuccess(User user);
        void onFailure(String message);
    }

    public interface RegisterCallback {
        void onSuccess(String message);
        void onFailure(String message);
    }
    public User findUserByEmail(String email) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("user");
        final User[] user = {null};

        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user[0] = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw new RuntimeException("Lỗi khi kết nối Firebase: " + databaseError.getMessage());
            }
        });

        if (user[0] == null) {
            throw new UsernameNotFoundException("Không tìm thấy user với email: " + email);
        }

        return user[0];
    }
}

