package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.Appointment;
import com.example.WebSiteDatLich.service.AppointmentService;
import com.example.WebSiteDatLich.service.DiagnosisService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final DiagnosisService diagnosisService;

    public AppointmentController(AppointmentService appointmentService, DiagnosisService diagnosisService) {
        this.appointmentService = appointmentService;
        this.diagnosisService = diagnosisService;
    }
    @GetMapping("/all")
    public String getAllAppointments(Model model) {
        try {
            CompletableFuture<List<Appointment>> allAppointmentsFuture = appointmentService.getAllAppointments();
            List<Appointment> allAppointments = allAppointmentsFuture.join(); // Chờ hoàn thành
            model.addAttribute("appointments", allAppointments);
            return "Appointments/all-appointments"; // Tên view để hiển thị danh sách
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "/error-page";
        }
    }
    /**
     * Hiển thị danh sách cuộc hẹn chưa được xác nhận
     */

    @GetMapping("/appointments")
    public String getAppointments(Model model) {
        CompletableFuture<List<Appointment>> appointmentsFuture = appointmentService.getUnconfirmedAppointmentsWithDetails();
        List<Appointment> appointments = appointmentsFuture.join(); // Chờ hoàn thành
        System.out.println("Appointments: " + appointments);
        model.addAttribute("appointments", appointments);
        return "/Appointments/appointment-list";
    }

    /**
     * Hiển thị danh sách cuộc hẹn đã được xác nhận
     */
    @GetMapping("/confirmed")
    public String getConfirmedAppointments(Model model) {
        try {
            CompletableFuture<List<Appointment>> confirmedAppointmentsFuture = appointmentService.getConfirmedAppointmentsWithDetails(1);
            List<Appointment> confirmedAppointments = confirmedAppointmentsFuture.join();
            System.out.println("Confirmed Appointments: " + confirmedAppointments);
            model.addAttribute("confirmedAppointments", confirmedAppointments);
            return "Appointments/confirmed-appointment-list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "/error-page";
        }
    }

    /**
     * Lấy chi tiết cuộc hẹn dựa trên ID
     */
    @GetMapping("/detail/{id}")
    @ResponseBody
    public Map<String, Object> getAppointmentDetails(@PathVariable("id") String appointmentId) {
        System.out.println("Requested Appointment ID: " + appointmentId); // Log kiểm tra
        Map<String, Object> response = new HashMap<>();
        try {
            CompletableFuture<Appointment> appointmentFuture = diagnosisService.getAppointmentById(appointmentId);
            Appointment appointment = appointmentFuture.join();

            response.put("success", true);
            response.put("appointment", appointment);
        } catch (Exception e) {
            System.err.println("Error retrieving appointment details: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    /**
     * Lưu thông tin chẩn đoán bệnh
     */
    @PostMapping("/diagnosis")
    @ResponseBody
    public Map<String, Object> saveDiagnosis(@RequestBody Map<String, String> detailData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String appointmentId = detailData.get("appointmentId");
            String diagnoseName = detailData.get("diagnoseName");
            String treatmentName = detailData.get("treatmentName");

            // Gọi DiagnosisService để lưu thông tin chẩn đoán
            diagnosisService.saveDiagnosis(appointmentId, diagnoseName, treatmentName).join();

            response.put("success", true);
            response.put("message", "Chẩn đoán bệnh đã được lưu thành công.");
        } catch (Exception e) {
            System.err.println("Error saving diagnosis: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
    @PostMapping("/api/appointments/confirm")
    public String confirmAppointment(@RequestParam("appointmentId") String appointmentId, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.confirmAppointment(appointmentId).join(); // Đợi hoàn thành
            redirectAttributes.addFlashAttribute("successMessage", "Cuộc hẹn đã được xác nhận thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xác nhận thất bại: " + ex.getMessage());
        }

        return "redirect:/appointments/appointments"; // Điều hướng lại trang danh sách cuộc hẹn
    }
    @PostMapping("/api/appointments/cancel")
    public String cancelAppointment(@RequestParam("appointmentId") String appointmentId, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelAppointment(appointmentId).join(); // Đợi hoàn thành
            redirectAttributes.addFlashAttribute("successMessage", "Cuộc hẹn đã bị hủy thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hủy cuộc hẹn thất bại: " + ex.getMessage());
        }

        return "redirect:/appointments/appointments"; // Điều hướng lại trang danh sách cuộc hẹn
    }
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
}
