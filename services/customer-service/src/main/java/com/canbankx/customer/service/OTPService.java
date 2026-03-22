package com.canbankx.customer.service;

import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.domain.OTPToken;
import com.canbankx.customer.repository.OTPTokenRepository;
import com.canbankx.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OTPService {

    private final OTPTokenRepository otpRepo;
    private final CustomerRepository custRepo;
    private final EmailService emailSvc;

    @Transactional
    public OTPToken generateOTP(UUID customerId, String deliveryMethod) {
        log.info("Generating OTP for customer: {} via {}", customerId, deliveryMethod);

        Customer cust = custRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        String otpCode = generateRandomOTP();
        String token = UUID.randomUUID().toString();

        OTPToken otp = OTPToken.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .token(token)
                .otpCode(otpCode)
                .status(OTPToken.OTPStatus.PENDING)
                .deliveryMethod(deliveryMethod)
                .attemptCnt(0)
                .build();

        otpRepo.save(otp);

        if ("EMAIL".equalsIgnoreCase(deliveryMethod)) {
            emailSvc.sendOTPEmail(cust.getEmail(), otpCode);
        } else if ("SMS".equalsIgnoreCase(deliveryMethod)) {
            log.info("SMS OTP sent to customer (simulated): {}", otpCode);
        }

        log.info("OTP generated with token: {}", token);
        return otp;
    }

    @Transactional
    public boolean verifyOTP(String otpToken, String otpCode) {
        log.info("Verifying OTP token: {}", otpToken);

        Optional<OTPToken> optOtp = otpRepo.findByToken(otpToken);
        if (optOtp.isEmpty()) {
            log.warn("OTP token not found: {}", otpToken);
            return false;
        }

        OTPToken otp = optOtp.get();

        if (otp.getStatus() == OTPToken.OTPStatus.EXPIRED) {
            log.warn("OTP token expired: {}", otpToken);
            return false;
        }

        if (otp.getStatus() == OTPToken.OTPStatus.LOCKED) {
            log.warn("OTP token locked due to too many attempts: {}", otpToken);
            return false;
        }

        if (Instant.now().isAfter(otp.getExpiresAt())) {
            otp.setStatus(OTPToken.OTPStatus.EXPIRED);
            otpRepo.save(otp);
            log.warn("OTP expired: {}", otpToken);
            return false;
        }

        otp.setAttemptCnt(otp.getAttemptCnt() + 1);

        if (otp.getAttemptCnt() > 3) {
            otp.setStatus(OTPToken.OTPStatus.LOCKED);
            otpRepo.save(otp);
            log.warn("OTP locked after 3 failed attempts: {}", otpToken);
            return false;
        }

        if (!otp.getOtpCode().equals(otpCode)) {
            otpRepo.save(otp);
            log.warn("OTP code mismatch for token: {}", otpToken);
            return false;
        }

        otp.setStatus(OTPToken.OTPStatus.VERIFIED);
        otp.setVerifiedAt(Instant.now());
        otpRepo.save(otp);

        log.info("OTP verified successfully: {}", otpToken);
        return true;
    }

    private String generateRandomOTP() {
        Random rand = new Random();
        int otpNum = 100000 + rand.nextInt(900000);
        return String.valueOf(otpNum);
    }
}
