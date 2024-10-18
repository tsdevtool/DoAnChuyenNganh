package com.example.WebSiteDatLich.controller;
import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public class AdminController {
    @Autowired
    private  final AdminService adminService;
    public  AdminController(AdminService adminService){
        this.adminService= adminService;
    }
    @GetMapping("/doctoradmin")
    public String showDoctorAdmin(Model model) {
        CompletableFuture<List<Doctor>> doctorsFuture = adminService.getAllDoctors();
        // Xử lý dữ liệu không đồng bộ
        List<Doctor> doctors = doctorsFuture.join();
        model.addAttribute("doctors", doctors);

        // Trả về view doctoradmin-list
        return "DoctorAdmin/doctoradmin-list";
    }
    // Phương thức để hiển thị thông tin chi tiết bác sĩ
    @GetMapping("/doctoradmin/{id}")
    public String showDoctorDetail(@PathVariable("id") Long doctorId, Model model) {
        CompletableFuture<Doctor> doctorFuture = adminService.getDoctorById(doctorId);
        Doctor doctor = doctorFuture.join();  // Chờ hoàn tất thông tin
        model.addAttribute("doctor", doctor);  // Truyền thông tin bác sĩ vào view
        return "DoctorAdmin/doctoradmin-detail";  // Trả về trang chi tiết bác sĩ
    }
    

}
