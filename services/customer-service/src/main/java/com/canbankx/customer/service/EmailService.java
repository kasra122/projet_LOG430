package com.canbankx.customer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    public void sendOTPEmail(String email, String otpCode) {
        log.info("Sending OTP email to: {} with code: {}", email, otpCode);
    }

    public void sendLoginNotification(String email, String device) {
        log.info("Sending login notification to: {} from device: {}", email, device);
    }
}
