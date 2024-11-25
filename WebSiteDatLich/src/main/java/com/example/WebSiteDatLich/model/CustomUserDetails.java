package com.example.WebSiteDatLich.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
    private final String name;
    private final String phone;
    private final int roleId;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities,
                             String name, String phone, int roleId) {
        super(username, password, authorities);
        this.name = name;
        this.phone = phone;
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public int getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        switch (roleId) {
            case 1:
                return "ROLE_USER";
            case 2:
                return "ROLE_ADMIN";
            case 3:
                return "ROLE_GUEST";
            default:
                return "ROLE_UNKNOWN";
        }
    }
}
