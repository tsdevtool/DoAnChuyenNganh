package com.example.WebSiteDatLich.controller;

import com.example.WebSiteDatLich.model.User;
import com.example.WebSiteDatLich.service.UserService;
import com.google.api.Authentication;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        userService.login(email, password, new UserService.LoginCallback() {
            @Override
            public void onSuccess(User user) {
                // Ánh xạ role_id thành role name
                String roleName = mapRoleIdToRoleName(user.getRole_id());

                // Tạo thông tin xác thực
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority(roleName))
                );

                // Lưu thông tin xác thực vào SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Lưu vào session để sử dụng trong View
                session.setAttribute("loggedInUser", user);

                redirectAttributes.addFlashAttribute("message", "Đăng nhập thành công!");
            }

            @Override
            public void onFailure(String errorMessage) {
                redirectAttributes.addFlashAttribute("error", errorMessage);
            }
        });

        return "redirect:/doctors"; // Chuyển hướng sau khi đăng nhập thành công
    }

    @PostMapping("/register")
    public String register(
            @RequestParam("password") String password,
            @RequestParam("name") String name,
            @RequestParam("sex") Boolean sex,
            @RequestParam("date_of_birth") String dateOfBirth,
            @RequestParam("email") String email,
            @RequestParam("address") String address,
            @RequestParam("phone") String phone,
            @RequestParam("avatar") MultipartFile avatarFile,
            RedirectAttributes redirectAttributes
    ) {
        User user = new User();
        user.setPassword(password);
        user.setName(name);
        user.setSex(sex);
        user.setDate_of_birth(dateOfBirth);
        user.setEmail(email);
        user.setAddress(address);
        user.setPhone(phone);

        userService.register(user, avatarFile, new UserService.RegisterCallback() {
            @Override
            public void onSuccess(String message) {
                redirectAttributes.addFlashAttribute("message", message);
            }

            @Override
            public void onFailure(String message) {
                redirectAttributes.addFlashAttribute("error", message);
            }
        });

        return "redirect:/login";
    }
    private String mapRoleIdToRoleName(int roleId) {
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

