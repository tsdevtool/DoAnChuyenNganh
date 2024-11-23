package com.example.WebSiteDatLich.model;

public class Department {
    public Integer getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(Integer department_id) {
        this.department_id = department_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getDiagnose_id() {
        return diagnose_id;
    }

    public void setDiagnose_id(Integer diagnose_id) {
        this.diagnose_id = diagnose_id;
    }

    private Integer department_id;
    private String name;
    private Integer diagnose_id;  // Foreign key to Diagnose
    private String image;
}
