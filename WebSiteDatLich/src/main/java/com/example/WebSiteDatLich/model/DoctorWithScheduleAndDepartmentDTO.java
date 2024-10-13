package com.example.WebSiteDatLich.model;

import java.util.List;

public class DoctorWithScheduleAndDepartmentDTO {
    private Doctor doctor;
    private List<Work_schedule> workSchedules;
    private Department department;

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public List<Work_schedule> getWorkSchedules() {
        return workSchedules;
    }

    public void setWorkSchedules(List<Work_schedule> workSchedules) {
        this.workSchedules = workSchedules;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
