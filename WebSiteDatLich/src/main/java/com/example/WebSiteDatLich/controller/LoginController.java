package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private UserService userService;

    // Login using email and password
    @PostMapping("/login")
    public CompletableFuture<String> login(@RequestParam String email, @RequestParam String password) {
        CompletableFuture<String> future = new CompletableFuture<>();

        userService.login(email, password, new UserService.LoginCallback() {
            @Override
            public void onSuccess(String message) {
                future.complete("Login successful!");
            }

            @Override
            public void onFailure(String message) {
                future.complete("Invalid email or password!");
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
