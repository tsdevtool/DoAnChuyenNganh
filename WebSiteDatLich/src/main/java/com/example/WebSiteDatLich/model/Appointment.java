package com.example.WebSiteDatLich.model;

public class Appointment {
    private Integer appointment_id;
    private Integer user_id;  // Foreign key to User
    private Integer work_schedule_id;  // Foreign key to Work_schedule

    public Integer getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(Integer appointment_id) {
        this.appointment_id = appointment_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getWork_schedule_id() {
        return work_schedule_id;
    }

    public void setWork_schedule_id(Integer work_schedule_id) {
        this.work_schedule_id = work_schedule_id;
    }

    public String getMedical_condition() {
        return medical_condition;
    }

    public void setMedical_condition(String medical_condition) {
        this.medical_condition = medical_condition;
    }

    public String getImage_medical_records() {
        return image_medical_records;
    }

    public void setImage_medical_records(String image_medical_records) {
        this.image_medical_records = image_medical_records;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    private String medical_condition;
    private String image_medical_records;
    private Boolean status;

}
