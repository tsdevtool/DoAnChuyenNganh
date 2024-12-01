package com.example.WebSiteDatLich.service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
//public void sendConfirmationEmail(String toEmail, String subject, String body) {
//    SimpleMailMessage message = new SimpleMailMessage();
//    message.setTo(toEmail);
//    message.setSubject(subject);
//    message.setText(body);
//    try {
//        mailSender.send(message);
//        System.out.println("Confirmation email sent to: " + toEmail);
//    } catch (Exception e) {
//        e.printStackTrace();
//        System.out.println("Failed to send email.");
//    }
//}
public void sendConfirmationEmail(String toEmail, String subject, String body) {
    MimeMessage message = mailSender.createMimeMessage();

    try {
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body, true); // `true` để gửi email HTML
        mailSender.send(message);

        System.out.println("Confirmation email sent to: " + toEmail);
    } catch (MessagingException e) {
        e.printStackTrace();
        System.out.println("Failed to send email.");
    }
}

}
