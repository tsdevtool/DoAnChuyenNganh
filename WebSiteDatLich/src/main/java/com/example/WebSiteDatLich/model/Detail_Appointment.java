package com.example.WebSiteDatLich.model;

public class Detail_Appointment {
    private Integer detail_appointment_id;

    public Integer getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(Integer doctor_id) {
        this.doctor_id = doctor_id;
    }

    public String getDiagnose_name() {
        return diagnose_name;
    }

    public void setDiagnose_name(String diagnose_name) {
        this.diagnose_name = diagnose_name;
    }

    public Integer getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(Integer appointment_id) {
        this.appointment_id = appointment_id;
    }

    public Integer getDetail_appointment_id() {
        return detail_appointment_id;
    }

    public void setDetail_appointment_id(Integer detail_appointment_id) {
        this.detail_appointment_id = detail_appointment_id;
    }

    public String getTreatment_name() {
        return treatment_name;
    }

    public void setTreatment_name(String treatment_name) {
        this.treatment_name = treatment_name;
    }

    private Integer doctor_id;  // Foreign key to Doctor
    private Integer appointment_id;  // Foreign key to Appointment
    private String diagnose_name;
    private String treatment_name;
}
