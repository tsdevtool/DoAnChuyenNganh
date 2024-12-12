package com.example.WebSiteDatLich.model;

public class Detail_Appointment {
    private String detail_appointment_id;

    public String getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(String doctor_id) {
        this.doctor_id = doctor_id;
    }

    public String getDiagnose_name() {
        return diagnose_name;
    }

    public void setDiagnose_name(String diagnose_name) {
        this.diagnose_name = diagnose_name;
    }

    public String getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(String appointment_id) {
        this.appointment_id = appointment_id;
    }

    public String getDetail_appointment_id() {
        return detail_appointment_id;
    }

    public void setDetail_appointment_id(String detail_appointment_id) {
        this.detail_appointment_id = detail_appointment_id;
    }

    public String getTreatment_name() {
        return treatment_name;
    }

    public void setTreatment_name(String treatment_name) {
        this.treatment_name = treatment_name;
    }

    private String doctor_id;  // Foreign key to Doctor
    private String appointment_id;  // Foreign key to Appointment
    private String diagnose_name;
    private String treatment_name;
}
