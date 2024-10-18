package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.Appointment;
import com.example.WebSiteDatLich.service.AppointmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.ui.Model;






@Controller
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Thay đổi phương thức này để không sử dụng join() (không bắt buộc)
//    @GetMapping("/appointments")
//    public String getAppointments(Model model) {
//        // Bắt đầu truy xuất dữ liệu bất đồng bộ
//        appointmentService.getUnconfirmedAppointmentsWithDetails()
//                .thenAccept(appointments -> {
//                    model.addAttribute("appointments", appointments);
//                })
//                .exceptionally(ex -> {
//                    // Xử lý lỗi, có thể thêm thông báo lỗi vào model nếu cần
//                    model.addAttribute("errorMessage", "Failed to load appointments.");
//                    return null;
//                });
//
//        return "Appointments/appointment-list"; // Trả về view
//    }
    @GetMapping("/appointments")
    public String getAppointments(Model model) {
        List<Appointment> appointments = appointmentService.getUnconfirmedAppointmentsWithDetails().join();
        System.out.println("Appointments: " + appointments);  // In danh sách ra để kiểm tra
        model.addAttribute("appointments", appointments);
        return "Appointments/appointment-list";
    }


    // API xác nhận cuộc hẹn
    @PostMapping("/api/appointments/confirm")
    public String confirmAppointment(@RequestParam("appointmentId") String appointmentId, Model model) {
        appointmentService.confirmAppointment(appointmentId)
                .thenApply(result -> {
                    model.addAttribute("message", "Appointment confirmed!");
                    return null;
                })
                .exceptionally(ex -> {
                    model.addAttribute("message", "Failed to confirm appointment.");
                    return null;
                });
        return "redirect:/appointments";  // Điều hướng lại trang danh sách cuộc hẹn
    }


}