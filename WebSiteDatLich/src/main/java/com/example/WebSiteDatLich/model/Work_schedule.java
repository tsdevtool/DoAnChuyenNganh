package com.example.WebSiteDatLich.model;

import java.util.List;

public class Work_schedule {
    private String work_schedule_id;
    private String schedule;
    private List<String> timeSlots; // Danh sách các khung giờ
    private String doctor_id;

    // Getter và Setter
    public String getWork_schedule_id() {
        return work_schedule_id;
    }

    public void setWork_schedule_id(String work_schedule_id) {
        this.work_schedule_id = work_schedule_id;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public List<String> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<String> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public String getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(String doctor_id) {
        this.doctor_id = doctor_id;
    }
}
