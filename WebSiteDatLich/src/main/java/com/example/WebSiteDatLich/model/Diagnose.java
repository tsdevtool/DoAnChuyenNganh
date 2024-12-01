package com.example.WebSiteDatLich.model;

public class Diagnose {
    public String getDiagnose_id() {
        return diagnose_id;
    }

    public void setDiagnose_id(String diagnose_id) {
        this.diagnose_id = diagnose_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(String department_id) {
        this.department_id = department_id;
    }

    private String diagnose_id;
    private String name;
    private String department_id;
}
