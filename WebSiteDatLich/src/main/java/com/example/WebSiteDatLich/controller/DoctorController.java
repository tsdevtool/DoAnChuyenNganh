package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Controller
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // Show list of doctors asynchronously
    @GetMapping("/doctors")
    public String getAllDoctors(Model model) {
        CompletableFuture<List<Doctor>> doctorsFuture = doctorService.getAllDoctors();

        doctorsFuture.thenAccept(doctors -> {
            model.addAttribute("doctors", doctors);
        }).join(); // Chờ cho tới khi dữ liệu được tải

        return "Doctor/doctor-list"; // Trả về view doctor-list.html
    }
}