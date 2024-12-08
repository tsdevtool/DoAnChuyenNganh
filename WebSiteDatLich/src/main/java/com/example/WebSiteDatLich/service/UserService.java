package com.example.WebSiteDatLich.service;


import com.example.WebSiteDatLich.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private final DatabaseReference databaseReference;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Inject PasswordEncoder để mã hóa mật khẩu
    public boolean passwordMatches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
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
                            callback.onSuccess(user.getUser_id());
                            return;
                        }
                    }
                    callback.onFailure("Invalid email or password!");
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
    public void getUserDetails(String userId, UserDetailsCallback callback) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Parse dữ liệu từ Firebase thành đối tượng User
                    User user = dataSnapshot.getValue(User.class);
                    callback.onSuccess(user);
                } else {
                    callback.onFailure("User not found with ID: " + userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onFailure("Error fetching user details: " + databaseError.getMessage());
            }
        });
    }
    public CompletableFuture<User> getUserDetails(String userId) {
        CompletableFuture<User> future = new CompletableFuture<>();
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    future.complete(user);
                } else {
                    future.completeExceptionally(new RuntimeException("User not found with ID: " + userId));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Error: " + databaseError.getMessage()));
            }
        });
        return future;
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
    public User findByEmail(String email) {
        CompletableFuture<User> future = new CompletableFuture<>();

        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        User user = childSnapshot.getValue(User.class);
                        future.complete(user); // Trả về người dùng
                        return;
                    }
                }
                future.complete(null); // Không tìm thấy người dùng
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException("Firebase query failed: " + databaseError.getMessage()));
            }
        });

        try {
            return future.get(); // Chờ dữ liệu Firebase trả về
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error retrieving user from Firebase", e);
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
    public interface UserDetailsCallback {
        void onSuccess(User user);
        void onFailure(String message);
    }
}