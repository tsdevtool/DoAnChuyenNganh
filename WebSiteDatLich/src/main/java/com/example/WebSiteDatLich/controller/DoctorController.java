package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.service.DoctorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }
    @GetMapping("/doctors")
    public String getAllDoctors(Model model) {
        CompletableFuture<List<Doctor>> doctorsFuture = doctorService.getAllDoctors();
        doctorsFuture.thenAccept(doctors -> {
            model.addAttribute("doctors", doctors);
        }).join(); // Chờ cho tới khi dữ liệu được tải
        return "Doctor/doctor-list"; // Trả về view doctor-list.html
    }
    @GetMapping("/doctor/details/{id}")
    public String getDoctorDetails(@PathVariable("id") String doctorId, Model model) {
        CompletableFuture<Doctor> doctorFuture = doctorService.getDoctorById(doctorId);
        doctorFuture.thenAccept(doctor -> model.addAttribute("doctor", doctor))
                .exceptionally(ex -> {
                    model.addAttribute("error", "Không thể tải thông tin bác sĩ: " + ex.getMessage());
                    return null;
                }).join();
        return "Doctor/doctor-details"; // Trả về view chi tiết
    }
}
