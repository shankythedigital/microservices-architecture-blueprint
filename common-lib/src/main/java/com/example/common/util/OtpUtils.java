package com.example.common.util;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.SecureRandom;

public class OtpUtils {

    private static final String DIGITS = "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int OTP_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 3;

    /**
     * Generates an OTP of the specified length.
     *
     * @param mobile The mobile number (can be used for logging or additional logic).
     * @param length The length of the OTP to generate.
     * @return A randomly generated OTP as a String.
     */
    public static String generateOtp(String mobile) {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return otp.toString();
    }

    /**
     * Validates the OTP input against the stored OTP code.
     *
     * @param otpInput   The OTP input provided by the user.
     * @param getOtpCode The stored OTP code to validate against.
     * @param passwordEncoder The PasswordEncoder instance used for validation.
     * @return True if the OTP is valid, false otherwise.
     */
    public static boolean validateOtp(String otpInput, String getOtpCode, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(otpInput, getOtpCode);
    }
}

