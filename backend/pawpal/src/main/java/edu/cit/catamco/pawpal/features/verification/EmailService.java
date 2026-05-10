package edu.cit.catamco.pawpal.features.verification;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationApproved(String toEmail, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("PawPal — Verification Approved! 🐾");
        message.setText(
                "Hi " + fullName + ",\n\n" +
                        "Great news! Your identity verification on PawPal has been approved.\n\n" +
                        "You can now access all features on the platform.\n\n" +
                        "Welcome to the PawPal community!\n\n" +
                        "— The PawPal Team"
        );
        mailSender.send(message);
    }

    public void sendVerificationRejected(String toEmail, String fullName, String adminComment) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("PawPal — Verification Update");
        message.setText(
                "Hi " + fullName + ",\n\n" +
                        "Unfortunately, your identity verification on PawPal was not approved.\n\n" +
                        "Reason: " + adminComment + "\n\n" +
                        "You may resubmit your verification request with the correct documents through the app.\n\n" +
                        "— The PawPal Team"
        );
        mailSender.send(message);
    }
}