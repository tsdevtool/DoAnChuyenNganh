package com.example.WebSiteDatLich.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Gửi email thông thường
     * @param toEmail Địa chỉ email người nhận
     * @param subject Chủ đề email
     * @param body Nội dung email (HTML hoặc văn bản)
     */
    public CompletableFuture<Void> sendEmail(String toEmail, String subject, String body) {
        return CompletableFuture.runAsync(() -> {
            MimeMessage message = mailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(body, true); // `true` để gửi email dạng HTML
                mailSender.send(message);

                System.out.println("Email sent successfully to: " + toEmail);
            } catch (MessagingException e) {
                System.err.println("Failed to send email to: " + toEmail);
                throw new RuntimeException("Failed to send email", e);
            }
        });
    }


}
