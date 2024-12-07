package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.Doctor;
import com.example.WebSiteDatLich.model.Work_schedule;
import com.example.WebSiteDatLich.service.DoctorService;
import com.example.WebSiteDatLich.service.WorkScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/api/work-schedules")
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;
    private final DoctorService doctorService;
    @Autowired
    public WorkScheduleController(WorkScheduleService workScheduleService ,DoctorService doctorService) {
        this.workScheduleService = workScheduleService;
        this.doctorService = doctorService;
    }
    @GetMapping("/add")
    public String showAddWorkScheduleForm(Model model) {
        CompletableFuture<List<Doctor>> doctorsFuture = doctorService.getAllDoctors();
        doctorsFuture.thenAccept(doctors -> model.addAttribute("doctors", doctors)).join(); // Wait for data to load
        return "AddWorkSchedulesDoctor/AddWorkDoctor"; // Return the view name
    }
    @PostMapping("/save")
    public CompletableFuture<String> saveWorkSchedules(@RequestBody List<Work_schedule> workSchedules) {
        return CompletableFuture.runAsync(() -> {
                    for (Work_schedule schedule : workSchedules) {
                        workScheduleService.saveWorkSchedule(schedule)
                                .exceptionally(ex -> {
                                    throw new RuntimeException("Lỗi khi lưu Work_schedule: " + ex.getMessage());
                                }).join();
                    }
                }).thenApply(aVoid -> "Lưu thành công tất cả Work_schedule!")
                .exceptionally(ex -> "Lỗi khi lưu Work_schedule: " + ex.getMessage());
    }
}
