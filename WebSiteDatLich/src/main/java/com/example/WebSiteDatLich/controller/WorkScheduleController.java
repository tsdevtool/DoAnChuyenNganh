package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.Work_schedule;
import com.example.WebSiteDatLich.service.WorkScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/work-schedules")
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;

    @Autowired
    public WorkScheduleController(WorkScheduleService workScheduleService) {
        this.workScheduleService = workScheduleService;
    }

    @PostMapping("/save")
    public CompletableFuture<String> saveWorkSchedule(@RequestBody Work_schedule workSchedule) {
        return workScheduleService.saveWorkSchedule(workSchedule)
                .thenApply(aVoid -> "Work Schedule đã được lưu thành công!")
                .exceptionally(ex -> "Lỗi khi lưu Work Schedule: " + ex.getMessage());
    }
}
