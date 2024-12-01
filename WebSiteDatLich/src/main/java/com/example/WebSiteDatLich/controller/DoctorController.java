package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.Appointment;
import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.service.DoctorService;
import com.example.WebSiteDatLich.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.multipart.MultipartFile;
@Controller
@RequestMapping("/doctors")
public class DoctorController {
    private final DoctorService doctorService;
    private final UserService userService;
    public DoctorController(DoctorService doctorService, UserService userService) {
        this.doctorService = doctorService;
        this.userService = userService;
    }

    @GetMapping
    public String getAllDoctors(Model model) {
        CompletableFuture<List<Doctor>> doctorsFuture = doctorService.getAllDoctors();
        doctorsFuture.thenAccept(doctors -> model.addAttribute("doctors", doctors)).join(); // Chờ dữ liệu được tải
        return "Doctor/doctor-list"; // Trả về view doctor-list.html
    }

    @GetMapping("/details/{id}")
    public String getDoctorDetails(@PathVariable("id") String doctorId, Model model, Principal principal) {
        // Lấy thông tin bác sĩ
        CompletableFuture<Doctor> doctorFuture = doctorService.getDoctorById(doctorId);
        doctorFuture.thenAccept(doctor -> model.addAttribute("doctor", doctor))
                .exceptionally(ex -> {
                    model.addAttribute("error", "Không thể tải thông tin bác sĩ: " + ex.getMessage());
                    return null;
                }).join();
        return "Doctor/doctor-details"; // Trả về view chi tiết
    }
    @PostMapping("/confirm")
    public String confirmBooking(@ModelAttribute Appointment appointment,
                                 @RequestParam("medicalImage") MultipartFile medicalImage,
                                 Model model) {

        CompletableFuture<String> bookingFuture = doctorService.confirmBooking(appointment, medicalImage);

        bookingFuture.thenAccept(message -> {
            model.addAttribute("message", "Đặt lịch thành công!");
        }).exceptionally(ex -> {
            model.addAttribute("error", "Lỗi khi đặt lịch: " + ex.getMessage());
            return null;
        }).join();

        // Điều hướng lại về danh sách bác sĩ (URL)
        return "redirect:/doctors";
    }
}