package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.Appointment;
import com.example.WebSiteDatLich.service.AppointmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

////////////////////////////////
import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
///////////////////////////////





@Controller
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }
    /////////////
    @GetMapping("/appointments")
    public String getAppointments(Model model) {
        List<Appointment> appointments = appointmentService.getUnconfirmedAppointmentsWithDetails().join();
        System.out.println("Appointments: " + appointments);  // In danh sách ra để kiểm tra
        model.addAttribute("appointments", appointments);
        return "Appointments/appointment-list";
    }
    ///////////////////
    @GetMapping("/appointments/confirmed")
    public String getConfirmedAppointments(Model model) {
        List<Appointment> confirmedAppointments = appointmentService.getConfirmedAppointmentsWithDetails(1).join();
        System.out.println("Confirmed Appointments: " + confirmedAppointments);  // In danh sách ra để kiểm tra
        model.addAttribute("confirmedAppointments", confirmedAppointments);
        return "Appointments/confirmed-appointment-list";
    }


    ///////////////////////
    @PostMapping("/api/appointments/confirm")
    public String confirmAppointment(@RequestParam("appointmentId") String appointmentId, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.confirmAppointment(appointmentId).join(); // Đợi hoàn thành
            redirectAttributes.addFlashAttribute("successMessage", "Cuộc hẹn đã được xác nhận thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xác nhận thất bại: " + ex.getMessage());
        }

        return "redirect:/appointments"; // Điều hướng lại trang danh sách cuộc hẹn
    }
    /////////////////////

    ///////////////////
    @PostMapping("/api/appointments/cancel")
    public String cancelAppointment(@RequestParam("appointmentId") String appointmentId, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelAppointment(appointmentId).join(); // Đợi hoàn thành
            redirectAttributes.addFlashAttribute("successMessage", "Cuộc hẹn đã bị hủy thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hủy cuộc hẹn thất bại: " + ex.getMessage());
        }

        return "redirect:/appointments"; // Điều hướng lại trang danh sách cuộc hẹn
    }
    ////////////////////
    @GetMapping("/api/appointments/{appointmentId}")
    @ResponseBody
    public ResponseEntity<Appointment> getAppointmentDetails(@PathVariable String appointmentId) {
        CompletableFuture<Appointment> appointmentFuture = appointmentService.getAppointmentById(appointmentId);
        Appointment appointment = appointmentFuture.join();
        if (appointment != null) {
            return ResponseEntity.ok(appointment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}