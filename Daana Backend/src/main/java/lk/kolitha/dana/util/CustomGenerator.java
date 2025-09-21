package lk.kolitha.dana.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
@Log4j2
public class CustomGenerator {

    // Reusable method to generate a custom 6-digit OTP
    public static String generateSixDigitOtp() {
        log.info("Generating six digit OTP");
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // ensures 6 digits (100000-999999)
        String value = String.valueOf(otp);
        log.info("Generated six digit OTP: {}", value);
        return value;
    }
}
