package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;

    // Endpoint để import 3 role mặc định
    @GetMapping("/import-roles")
    public String importRoles() {
        roleService.importDefaultRoles();
        return "Import các vai trò mặc định thành công!";
    }
}