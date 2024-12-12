package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.Appointment;
import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private UserService userService;
    private final AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public LoginController(AuthenticationManager authenticationManager) {
        this.authenticationManager   = authenticationManager;
    }
    // Login using email and password
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        System.out.println("Login attempt for email: " + loginRequest.getEmail());

        // Kiểm tra thông tin người dùng
        User user = userService.findByEmail(loginRequest.getEmail());
        if (user == null || !userService.passwordMatches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        // So khớp mật khẩu
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        // Tạo đối tượng Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        // Cập nhật SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        System.out.println("User authenticated and SecurityContext updated.");

        // Trả về thông tin người dùng
        return ResponseEntity.ok(user);
    }

    // Register new user
    @PostMapping("/register")
    public CompletableFuture<String> register(
            @RequestParam("password") String password,
            @RequestParam("name") String name,
            @RequestParam("sex") Boolean sex,
            @RequestParam("date_of_birth") String dateOfBirth,
            @RequestParam("email") String email,
            @RequestParam("address") String address,
            @RequestParam("phone") String phone,
            @RequestParam("avatar") MultipartFile avatarFile, // Nhận tệp ảnh
            RedirectAttributes redirectAttributes
    ) {
        CompletableFuture<String> future = new CompletableFuture<>();

        // Create new User object
        User user = new User();
        user.setPassword(password);
        user.setName(name);
        user.setSex(sex);
        user.setDate_of_birth(dateOfBirth);
        user.setEmail(email);
        user.setAddress(address);
        user.setPhone(phone);

        // Process registration with UserService
        userService.register(user, avatarFile, new UserService.RegisterCallback() {
            @Override
            public void onSuccess(String message) {
                redirectAttributes.addFlashAttribute("message", "Registration successful! Please log in.");
                future.complete("redirect:/login");
            }

            @Override
            public void onFailure(String message) {
                redirectAttributes.addFlashAttribute("error", message);
                future.complete("redirect:/register");
            }
        });

        return future;
    }
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Xử lý logout
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());

        // Trả về phản hồi
        return ResponseEntity.ok("Logout successful");
    }
    @GetMapping("/profile")
    public String getUserProfile(@RequestParam("userId") String userId, Model model) {
        System.out.println("Received userId: " + userId);
        if (userId == null || userId.isEmpty()) {
            model.addAttribute("error", "Thiếu thông tin userId!");
            return "error"; // Trả về trang lỗi nếu không có userId
        }

        try {
            // Lấy thông tin người dùng
            CompletableFuture<User> userFuture = userService.getCurrentUserProfile(userId);
            User user = userFuture.get();
            System.out.println("User fetched: " + user);
            model.addAttribute("user", user);

            // Lấy danh sách cuộc hẹn
            CompletableFuture<List<Appointment>> appointmentsFuture = userService.getUserAppointments(userId);
            List<Appointment> appointments = appointmentsFuture.get();
            model.addAttribute("appointments", appointments);

            return "Customer/profile"; // Trả về view profile
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin người dùng hoặc danh sách cuộc hẹn: " + e.getMessage());
            return "error"; // Trả về trang lỗi nếu có vấn đề
        }
    }
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "password", required = false) String newPassword,
            @RequestParam("userId") String userId,
            Model model) {
        try {
            // Lấy thông tin người dùng hiện tại từ Firebase
            User existingUser = userService.getCurrentUserProfile(userId).get();

            // Cập nhật các trường thay đổi
            if (name != null) existingUser.setName(name);
            if (email != null) existingUser.setEmail(email);
            if (address != null) existingUser.setAddress(address);
            if (phone != null) existingUser.setPhone(phone);

            // Gọi service để cập nhật thông tin (bao gồm mật khẩu nếu có)
            userService.updateUserProfile(existingUser, newPassword);

            // Cập nhật lại thông tin trong model để hiển thị
            model.addAttribute("user", existingUser);
            model.addAttribute("success", "Thông tin cá nhân đã được cập nhật thành công!");
            return "redirect:/api/auth/profile?userId=" + userId;
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi cập nhật thông tin: " + e.getMessage());
            return "error";
        }
    }
    @PostMapping("/profile/update-password")
    public String updatePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("userId") String userId,
            Model model) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "redirect:/api/auth/profile?userId=" + userId;
            }

            User user = userService.getCurrentUserProfile(userId).get();
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                model.addAttribute("error", "Mật khẩu hiện tại không đúng!");
                return "redirect:/api/auth/profile?userId=" + userId;
            }

            // Mã hóa mật khẩu mới
            String encodedPassword = passwordEncoder.encode(newPassword);
            userService.updateUserProfile(user, encodedPassword);

            model.addAttribute("success", "Mật khẩu đã được thay đổi thành công!");
            return "redirect:/api/auth/profile?userId=" + userId;
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi thay đổi mật khẩu: " + e.getMessage());
            return "redirect:/api/auth/profile?userId=" + userId;
        }
    }

}
