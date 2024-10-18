package com.example.WebSiteDatLich.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    // Gửi email xác nhận cuộc hẹn
    public CompletableFuture<Void> sendConfirmationEmail(String appointmentId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Lấy email của người dùng từ Firebase dựa trên appointmentId
        String userEmail = getUserEmailByAppointmentId(appointmentId);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject("Xác nhận cuộc hẹn");
        message.setText("Cuộc hẹn của bạn đã được xác nhận!");

        // Gửi email
        emailSender.send(message);

        future.complete(null); // Gửi email thành công
        return future;
    }

    // Giả sử hàm này lấy email của người dùng từ Firebase dựa trên appointmentId
    private String getUserEmailByAppointmentId(String appointmentId) {
        // Thực hiện logic để lấy email của user từ Firebase
        return "user@example.com"; // Thay bằng email thực tế từ dữ liệu
    }
}
