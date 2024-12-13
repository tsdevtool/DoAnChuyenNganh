package com.example.WebSiteDatLich.config;

import com.example.WebSiteDatLich.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
@EnableAsync
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterAfter(new SecurityContextPersistenceFilter(), UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))// Vô hiệu hóa CSRF nếu không cần
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/api/auth/register", "/api/auth/login","/doctors", "/appointments","/doctors/details/**","/doctors/confirm").permitAll()
                        .requestMatchers("/importDoctorWithScheduleAndDepartment","/admin/**",
                                "/api/work-schedules/**","/user/{userId}",
                                "/doctoradmin/**","/departmentadmin/**",
                                "/departmentadmin/delete", "/import-data", "/useradmin/**",
                                "/positionadmin/**","/saffadmin/**", "/diagnoseadmin/**",
                                "/appointments","/appointments/confirmed",
                                "/api/appointments/confirm/**","/api/appointments/cancel/**","/appointments/**").hasRole("ADMIN")  // Chỉ ADMIN có thể import bác sĩ// Cho phép truy cập công khai vào view đăng ký và đăng nhập
                        .anyRequest().authenticated()  // Các yêu cầu khác phải xác thực
                )
                .formLogin(form -> form
                        .loginPage("/login")  // URL đến trang đăng nhập tùy chỉnh
                        .loginProcessingUrl("/perform_login")  // URL xử lý đăng nhập
                        .defaultSuccessUrl("/doctors", true)  // Chuyển hướng sau khi đăng nhập thành công
                        .failureUrl("/login?error=true")  // Chuyển hướng nếu đăng nhập thất bại
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")  // URL xử lý đăng xuất
                        .deleteCookies("JSESSIONID")  // Xóa cookie khi đăng xuất
                        .logoutSuccessUrl("/login?logout=true")  // Chuyển hướng sau khi đăng xuất thành công
                        .permitAll()
                )
                .httpBasic(Customizer.withDefaults());  // Sử dụng Basic Authentication

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public HttpFirewall allowUrlEncodedDoubleSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true); // Cho phép dấu "//"
        return firewall;
    }
}