package com.example.WebSiteDatLich.config;

import com.example.WebSiteDatLich.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    // Cấu hình AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder);
        return builder.build();
    }

    // Cấu hình SecurityFilterChain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt bảo vệ CSRF (nên bật nếu cần thiết)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/api/auth/**", "/doctors", "/doctors/details/**","/import-roles").permitAll()
                        .requestMatchers("/importDoctorWithScheduleAndDepartment").hasRole("ADMIN") // Chỉ ADMIN được phép
                        .anyRequest().authenticated() // Các request khác yêu cầu xác thực
                )
                .formLogin(form -> form
                        .loginPage("/login") // Trang đăng nhập tùy chỉnh
                        .loginProcessingUrl("/perform_login") // URL xử lý đăng nhập
                        .defaultSuccessUrl("/doctors", true) // Chuyển hướng sau khi đăng nhập thành công
                        .failureUrl("/login?error=true") // Chuyển hướng khi đăng nhập thất bại
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL xử lý đăng xuất
                        .deleteCookies("JSESSIONID") // Xóa cookie phiên
                        .logoutSuccessUrl("/login?logout=true") // Chuyển hướng sau khi đăng xuất
                        .permitAll()
                );

        return http.build();
    }

    // Bean PasswordEncoder để mã hóa mật khẩu

}
