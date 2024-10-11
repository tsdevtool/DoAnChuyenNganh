package com.example.WebSiteDatLich.model;

public class Work_schedule {
    private Integer work_schedule_id;
    private String  schedule;
    private String  time;
    private Integer doctor_id;
    public Integer getWork_schedule_id() {
        return work_schedule_id;
    }

    public void setWork_schedule_id(Integer work_schedule_id) {
        this.work_schedule_id = work_schedule_id;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getDoctor_id() {
        return doctor_id;
    }

    public void setDoctor_id(Integer doctor_id) {
        this.doctor_id = doctor_id;
    }


}
