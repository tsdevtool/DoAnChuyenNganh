package com.example.WebSiteDatLich.model;

import groovy.lang.GString;

public class Appointment {
    private String appointment_id;
    private String user_id;  // Foreign key to User
    private String work_schedule_id;  // Foreign key to Work_schedule
    private String medical_condition;
    private String image_medical_records;
    private Integer status;
    /////////////////////////////////////////////////////////////////////////
    private String timeSlot; // Lưu mã timeSlot khách hàng chọn

    // Additional fields for displaying details
    private String patientName;  // Name of the patient from the User table
    private String patientAddress;
    private String phoneNumber;  // Phone number of the patient from the User table
    private String appointmentDate;  // Appointment date from Work_schedule table
    private String appointmentTime;  // Appointment time from Work_schedule table
    private String patineSex;
    private String DoctorName;
    private String diagnoseName;
    private String treatmentName;
    private String patientEmail;




    public String getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(String appointment_id) {
        this.appointment_id = appointment_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getWork_schedule_id() {
        return work_schedule_id;
    }

    public void setWork_schedule_id(String work_schedule_id) {
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    // Getters and setters for the additional fields

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    ////////////////////////////////////////
    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }


    //gán tạm thời để lấy thông tin
    public String getDoctorName() {
        return DoctorName;
    }

    public void setDoctorName(String doctorName) {
        this.DoctorName = doctorName;
    }

    public String getPatientSex() {
        return patineSex;
    }

    public void setPatientSex(String patientSex) {
        this.patineSex = patientSex;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    public String getDiagnoseName() {
        return diagnoseName;
    }

    public void setDiagnoseName(String diagnoseName) {
        this.diagnoseName = diagnoseName;
    }

    public String getTreatmentName() {
        return treatmentName;
    }

    public void setTreatmentName(String treatmentName) {
        this.treatmentName = treatmentName;
    }


    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }



}