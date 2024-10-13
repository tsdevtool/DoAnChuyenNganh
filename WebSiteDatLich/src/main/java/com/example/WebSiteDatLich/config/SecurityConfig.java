package com.example.WebSiteDatLich.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Vô hiệu hóa CSRF nếu không cần
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**",  "/images/**", "/uploads/**", "/js/**", "/dashboard","/doctors", "/api/auth/register", "/api/auth/login").permitAll()  // Cho phép truy cập công khai vào view đăng ký và đăng nhập
                        .anyRequest().authenticated()  // Các yêu cầu khác phải xác thực
                )
                .formLogin(form -> form
                        .loginPage("/login")  // URL đến trang đăng nhập tùy chỉnh
                        .loginProcessingUrl("/perform_login")  // URL xử lý đăng nhập
                        .defaultSuccessUrl("/home", true)  // Chuyển hướng sau khi đăng nhập thành công
                        .failureUrl("/login?error=true")  // Chuyển hướng nếu đăng nhập thất bại
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/perform_logout")  // URL xử lý đăng xuất
                        .deleteCookies("JSESSIONID")  // Xóa cookie khi đăng xuất
                        .logoutSuccessUrl("/login?logout=true")  // Chuyển hướng sau khi đăng xuất thành công
                        .permitAll()
                )
                .httpBasic(Customizer.withDefaults());  // Sử dụng Basic Authentication

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Sử dụng mã hóa BCrypt để bảo mật mật khẩu
    }
}
