package com.example.WebSiteDatLich.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/register")
    public String showRegisterForm() {
        return "Login/register";  // Trả về file register.html trong thư mục Login
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "Login/login";  // Trả về file login.html trong thư mục Login
    }
    @GetMapping("/dashboard")
    public String showDashboard(){
        return"dashboard";
    }
}
