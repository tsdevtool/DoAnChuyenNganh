package com.example.WebSiteDatLich.service;

import com.example.WebSiteDatLich.model.CustomUserDetails;
import com.example.WebSiteDatLich.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Email không tồn tại: " + email);
        }

        // Ánh xạ role_id thành role name
        String roleName = mapRoleIdToRoleName(user.getRole_id());

        // Tạo đối tượng CustomUserDetails
        return new CustomUserDetails(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(roleName)),
                user.getName(),
                user.getPhone(),
                user.getRole_id()
        );
    }
    public String mapRoleIdToRoleName(int roleId) {
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
