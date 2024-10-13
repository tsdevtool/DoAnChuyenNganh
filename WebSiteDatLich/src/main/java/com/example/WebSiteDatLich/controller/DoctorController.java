package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // Endpoint để hiển thị danh sách bác sĩ
    @GetMapping("/doctors")
    public String showDoctors(Model model) {
        try {
            List<Doctor> doctors = doctorService.getDoctors();
            model.addAttribute("doctors", doctors);
        } catch (InterruptedException e) {
            model.addAttribute("error", "Error fetching doctor list.");
        }

        return "doctor/doctor_list"; // Thymeleaf template để hiển thị danh sách
    }
}