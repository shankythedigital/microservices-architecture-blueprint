
package com.example.common.converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OtpConverter {
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int OTP_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 3;

    public String generateOtp(String mobile) {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        String otpStr = String.valueOf(otp);

        return otpStr;
    }

    public boolean validateOtp(String otpInput, String getOtpCode) {
        boolean valid = passwordEncoder.matches(otpInput, getOtpCode);
        return valid;
    }
}



