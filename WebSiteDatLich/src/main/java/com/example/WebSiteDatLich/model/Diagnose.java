package com.example.WebSiteDatLich.model;

public class Diagnose {
    public Integer getDiagnose_id() {
        return diagnose_id;
    }

    public void setDiagnose_id(Integer diagnose_id) {
        this.diagnose_id = diagnose_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTreatment_id() {
        return treatment_id;
    }

    public void setTreatment_id(Integer treatment_id) {
        this.treatment_id = treatment_id;
    }

    private Integer diagnose_id;
    private String name;
    private Integer treatment_id;
}
