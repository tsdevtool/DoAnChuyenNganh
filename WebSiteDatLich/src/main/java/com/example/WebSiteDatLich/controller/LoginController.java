package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private UserService userService;

    // Login using email and password
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<?>> login(@RequestBody User loginRequest) {
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();

        userService.login(loginRequest.getEmail(), loginRequest.getPassword(), new UserService.LoginCallback() {
            @Override
            public void onSuccess(String userId) {
                CompletableFuture<User> userFuture = userService.getUserDetails(userId);
                userFuture.thenAccept(user -> {
                    future.complete(ResponseEntity.ok().body(user));
                }).exceptionally(ex -> {
                    future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching user details: " + ex.getMessage()));
                    return null;
                });
            }

            @Override
            public void onFailure(String message) {
                future.complete(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + message));
            }
        });

        return future;
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
