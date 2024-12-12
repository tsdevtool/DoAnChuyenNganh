package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.Appointment;
import com.example.WebSiteDatLich.service.AppointmentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import org.springframework.ui.Model;

////////////////////////////////

///////////////////////////////




@CrossOrigin(origins = "*")
@Controller
//@RestController
//@RequestMapping("/api/appointments")
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
        return "/Appointments/appointment-list";
    }
    ///////////////////
    @GetMapping("/appointments/confirmed")
    public String getConfirmedAppointments(Model model) {
        List<Appointment> confirmedAppointments = appointmentService.getConfirmedAppointmentsWithDetails(1).join();
        System.out.println("Confirmed Appointments: " + confirmedAppointments);  // In danh sách ra để kiểm tra
        model.addAttribute("confirmedAppointments", confirmedAppointments);
        return "/Appointments/confirmed-appointment-list";
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
    /////////////////////////////////////////////////////////
//    @GetMapping("/appointments/confirmed/{id}")
//    public String getAppointmentDetails(@PathVariable String id, Model model) {
//        System.out.println("Received appointment ID: " + id);
//        try {
//            Appointment appointment = appointmentService.getAppointmentById(id).join();
//            System.out.println("Fetched appointment: " + appointment);
//            model.addAttribute("appointment", appointment);
//            return "Appointments/appointment-details";
//        } catch (Exception e) {
//            System.out.println("Error fetching appointment details: " + e.getMessage());
//            model.addAttribute("error", "Không thể tìm thấy thông tin cuộc hẹn");
//            return "error";
//        }
//    }
    @GetMapping("/appointments/confirmed/{id}")
    public String getAppointmentDetails(@PathVariable String id, Model model) {
        System.out.println("Received appointment ID: " + id);
        try {
            Appointment appointment = appointmentService.getAppointmentById(id).join();
            System.out.println("Fetched appointment: " + appointment);
            System.out.println("Patient Name: " + appointment.getPatientName());
            System.out.println("Doctor Name: " + appointment.getDoctorName());
            System.out.println("Appointment Date: " + appointment.getAppointmentDate());
            model.addAttribute("appointment", appointment);
            return "templates/Appointments/appointment-details";
        } catch (Exception e) {
            System.out.println("Error fetching appointment details: " + e.getMessage());
            model.addAttribute("error", "Không thể tìm thấy thông tin cuộc hẹn");
            return "error";
        }
    }








    @PostMapping("/appointments/update-details")
    public String updateAppointmentDetails(@RequestParam String appointmentId,
                                           @RequestParam String diagnoseName,
                                           @RequestParam String treatmentName,
                                           RedirectAttributes redirectAttributes) {
        try {
            // Gọi service để cập nhật chi tiết cuộc hẹn
            appointmentService.updateDetailAppointment(appointmentId, diagnoseName, treatmentName).join();
            redirectAttributes.addFlashAttribute("successMessage", "Chi tiết cuộc hẹn đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật chi tiết cuộc hẹn: " + e.getMessage());
        }

        return "redirect:/appointments/confirmed/" + appointmentId;
    }


}