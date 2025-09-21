package lk.kolitha.dana.util;

import jakarta.mail.internet.MimeMessage;
import lk.kolitha.dana.exception.CustomServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class DaanaSESEmailSender {

    @Value("${daana.mail.from}")
    private String fromEmail;

    private final JavaMailSender javaMailSender;

    public void sendHtmEmail(String to, String subject, String text) {
        try {
            this.isValidEmail(to);
            MimeMessage msg = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(fromEmail, "Daana.lk");
            helper.setReplyTo(fromEmail);
            helper.setText("Please view this email in an HTML-compatible email client.", text);

            javaMailSender.send(msg);
            log.info("Method sendHtmEmail : mail successfully dispatched to {}", to);
        } catch (Exception e) {
            log.error("Method sendHtmEmail : mail sending failed - ", e);
            throw new CustomServiceException("Email Sending Failed!");
        }
    }

    void isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (email == null || !email.matches(emailRegex)) {
            log.error("Invalid email address: {}", email);
            throw new CustomServiceException("Invalid email address");
        }
    }

}
