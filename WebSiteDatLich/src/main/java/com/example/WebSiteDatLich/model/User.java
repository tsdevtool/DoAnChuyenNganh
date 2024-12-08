package com.example.WebSiteDatLich.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {
    private String user_id;
    private String password;
    private String name;
    private Boolean sex;
    private String date_of_birth;
    private String email;
    private String address;
    private String phone;
    private String avatar;
    private Integer role_id;

    // Enum để ánh xạ từ role_id
    @JsonIgnore // Không serialize trường này nếu trả về JSON
    private Role role;

    // Getters và Setters
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getSex() {
        return sex;
    }

    public void setSex(Boolean sex) {
        this.sex = sex;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getRole_id() {
        return role_id;
    }

    public void setRole_id(Integer role_id) {
        this.role_id = role_id;

        // Cập nhật giá trị của Role Enum dựa trên role_id
        this.role = Role.fromId(role_id);
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;

        // Nếu Role Enum được set, tự động cập nhật role_id
        this.role_id = role.getId();
    }
}