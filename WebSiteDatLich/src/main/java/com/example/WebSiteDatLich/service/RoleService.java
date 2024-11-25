package com.example.WebSiteDatLich.service;

import com.example.WebSiteDatLich.model.Role;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final DatabaseReference databaseReference;

    public RoleService() {
        // Trỏ tới node "roles" trong Firebase
        this.databaseReference = FirebaseDatabase.getInstance().getReference("roles");
    }

    // Hàm lưu một role
    public void saveRole(Role role) {
        String roleId = String.valueOf(role.getRole_id()); // Dùng role_id làm khóa
        databaseReference.child(roleId).setValueAsync(role);



    }

    // Hàm import 3 role mặc định
    public void importDefaultRoles() {
        Role userRole = new Role();
        userRole.setRole_id(1);
        userRole.setName("ROLE_USER");

        Role adminRole = new Role();
        adminRole.setRole_id(2);
        adminRole.setName("ROLE_ADMIN");

        Role guestRole = new Role();
        guestRole.setRole_id(3);
        guestRole.setName("ROLE_GUEST");

        saveRole(userRole);
        saveRole(adminRole);
        saveRole(guestRole);
    }
}