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

    @PostMapping("/login")
    public CompletableFuture<String> login(@RequestParam String userName, @RequestParam String password) {
        CompletableFuture<String> future = new CompletableFuture<>();

        userService.login(userName, password, new UserService.LoginCallback() {
            @Override
            public void onSuccess(String message) {
                future.complete("Login successful!");
            }

            @Override
            public void onFailure(String message) {
                future.complete("Invalid username or password!");
            }
        });

        return future;
    }

    @PostMapping("/register")
    public CompletableFuture<String> register(
            @RequestParam("user_name") String userName,
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

        // Tạo đối tượng User
        User user = new User();
        user.setUser_name(userName);
        user.setPassword(password);
        user.setName(name);
        user.setSex(sex);
        user.setDate_of_birth(dateOfBirth);
        user.setEmail(email);
        user.setAddress(address);
        user.setPhone(phone);

        // Xử lý đăng ký với UserService
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