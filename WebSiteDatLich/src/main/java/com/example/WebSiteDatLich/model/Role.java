package com.example.WebSiteDatLich.model;

import org.springframework.security.core.GrantedAuthority;

public class Role implements GrantedAuthority {
    private Integer role_id;
    private String name;

    public Integer getRole_id() {
        return role_id;
    }

    public void setRole_id(Integer role_id) {
        this.role_id = role_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getAuthority() {
        return name; // Trả về tên role, ví dụ: "ROLE_ADMIN" hoặc "ROLE_USER"
    }
}
