package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private UserService userService;
    private final AuthenticationManager authenticationManager;
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
}
