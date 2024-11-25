package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.CustomUserDetails;
import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.service.DoctorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }
//    @GetMapping()
//    public String getAllDoctors(Model model) {
//        CompletableFuture<List<Doctor>> doctorsFuture = doctorService.getAllDoctors();
//        doctorsFuture.thenAccept(doctors -> {
//            model.addAttribute("doctors", doctors);
//        }).join(); // Chờ cho tới khi dữ liệu được tải
//        return "Doctor/doctor-list"; // Trả về view doctor-list.html
//    }
@GetMapping()
public String getAllDoctors(Model model) {
    // Lấy thông tin người dùng hiện tại từ SecurityContextHolder
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();

    if (principal instanceof CustomUserDetails) {
        // Nếu đã đăng nhập, truyền thông tin người dùng vào model
        CustomUserDetails userDetails = (CustomUserDetails) principal;
        model.addAttribute("loggedInUser", userDetails);
    } else {
        // Nếu chưa đăng nhập, truyền giá trị null vào model
        model.addAttribute("loggedInUser", null);
    }

    // Lấy danh sách bác sĩ
    CompletableFuture<List<Doctor>> doctorsFuture = doctorService.getAllDoctors();
    doctorsFuture.thenAccept(doctors -> {
        model.addAttribute("doctors", doctors);
    }).join(); // Chờ cho tới khi dữ liệu được tải

    return "Doctor/doctor-list"; // Trả về view doctor-list.html
}
    @GetMapping("/details/{id}")
    public String doctorDetails(@PathVariable("id") String doctorId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            model.addAttribute("loggedInUser", userDetails);
        } else {
            model.addAttribute("loggedInUser", null);
        }

        // Lấy thông tin bác sĩ từ database
        Doctor doctor = doctorService.getDoctorById(doctorId).join();
        model.addAttribute("doctor", doctor);

        return "Doctor/doctor-details";
    }
}
