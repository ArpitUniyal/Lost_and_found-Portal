package com.lostandfound.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger =
            LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOwnerNotification(
            String ownerEmail,
            String ownerName,
            String itemName
    ) {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            logger.warn("Cannot send owner notification: owner email is missing");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(ownerEmail);
            message.setSubject("Someone may have found your lost item");

            message.setText(
                    "Hello " + (ownerName != null ? ownerName : "") + ",\n\n" +
                    "A found item may match your lost item"
                    + (itemName != null ? " \"" + itemName + "\"" : "")
                    + ".\n\n" +
                    "Please log in to the Campus Lost & Found portal "
                    + "to review the details.\n\n" +
                    "Regards,\n" +
                    "Campus Lost & Found"
            );

            mailSender.send(message);

            logger.info("Owner notification email sent to {}", ownerEmail);

        } catch (Exception e) {
            logger.error(
                    "Failed to send owner notification email to {}",
                    ownerEmail,
                    e
            );
        }
    }

    public void sendClaimApprovedNotification(
            String finderEmail,
            String finderName,
            String itemName,
            String location
    ) {
        if (finderEmail == null || finderEmail.isBlank()) {
            logger.warn("Cannot send claim approval email: finder email is missing");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(finderEmail);
            message.setSubject("Claim approved for your found item");

            message.setText(
                    "Hello " + (finderName != null ? finderName : "") + ",\n\n" +
                    "The owner has successfully verified the claim "
                    + "for the item you reported"
                    + (itemName != null ? " \"" + itemName + "\"" : "")
                    + (location != null ? " at " + location : "")
                    + ".\n\n" +
                    "The claim has been approved. "
                    + "Please log in to the Campus Lost & Found portal "
                    + "to view the claim details and proceed with the handover.\n\n" +
                    "Thank you for helping return the lost item.\n\n" +
                    "Regards,\n" +
                    "Campus Lost & Found"
            );

            mailSender.send(message);

            logger.info("Claim approval email sent to {}", finderEmail);

        } catch (Exception e) {
            logger.error(
                    "Failed to send claim approval email to {}",
                    finderEmail,
                    e
            );
        }
    }

    public void sendPasswordResetEmail(
        String recipientEmail,
        String recipientName,
        String resetLink
) {
    if (recipientEmail == null || recipientEmail.isBlank()) {
        logger.warn("Cannot send password reset email: recipient email is missing");
        return;
    }

    try {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("Reset Your Lost & Found Password");

        message.setText(
                "Hello " + (recipientName != null ? recipientName : "") + ",\n\n"
                + "We received a request to reset your Lost & Found account password.\n\n"
                + "Click the link below to create a new password:\n\n"
                + resetLink + "\n\n"
                + "This link will expire in 15 minutes.\n\n"
                + "If you did not request a password reset, you can safely ignore this email.\n\n"
                + "Regards,\n"
                + "Campus Lost & Found"
        );

        mailSender.send(message);

        logger.info("Password reset email sent to {}", recipientEmail);

    } catch (Exception e) {
        logger.error(
                "Failed to send password reset email to {}",
                recipientEmail,
                e
        );
    }
}
}