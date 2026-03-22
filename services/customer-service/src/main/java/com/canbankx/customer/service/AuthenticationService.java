package com.canbankx.customer.service;

import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.domain.DeviceRegistration;
import com.canbankx.customer.domain.OTPToken;
import com.canbankx.customer.domain.Session;
import com.canbankx.customer.dto.LoginRequest;
import com.canbankx.customer.dto.LoginResponse;
import com.canbankx.customer.dto.MFAVerificationRequest;
import com.canbankx.customer.dto.MFAVerificationResponse;
import com.canbankx.customer.repository.CustomerRepository;
import com.canbankx.customer.repository.DeviceRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final CustomerRepository custRepo;
    private final SessionService sessService;
    private final OTPService otpService;
    private final DeviceRegistrationRepository devRepo;
    private final BCryptPasswordEncoder pwdEncoder;

    @Transactional
    public LoginResponse login(LoginRequest req, String ipAddr, String userAgent) {
        log.info("Login attempt for email: {}", req.getEmail());

        Optional<Customer> optCust = custRepo.findByEmail(req.getEmail());
        if (optCust.isEmpty()) {
            log.warn("Login failed: customer not found for email: {}", req.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        Customer cust = optCust.get();

        if (!pwdEncoder.matches(req.getPwd(), cust.getPassword())) {
            log.warn("Login failed: invalid password for: {}", req.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        if (cust.getKycStatus() != Customer.KycStatus.VERIFIED) {
            log.warn("Login failed: KYC not verified for: {}", req.getEmail());
            throw new RuntimeException("KYC not verified");
        }

        UUID deviceId = null;
        if (req.getDeviceId() != null) {
            Optional<DeviceRegistration> optDev = devRepo.findByCustomerIdAndDeviceId(cust.getId(), req.getDeviceId());
            
            if (optDev.isPresent() && optDev.get().getIsTrusted()) {
                deviceId = optDev.get().getId();
                log.info("Device is trusted, skipping MFA for: {}", cust.getId());
                Session sess = sessService.createSession(cust.getId(), ipAddr, deviceId);
                sessService.verifyMFA(sess.getSessionToken());
                
                return LoginResponse.builder()
                        .msg("Login successful")
                        .sessionToken(sess.getSessionToken())
                        .mfaRequired(false)
                        .build();
            } else {
                deviceId = registerDevice(cust.getId(), req.getDeviceId(), req.getDeviceName(), ipAddr, userAgent);
            }
        }

        Session sess = sessService.createSession(cust.getId(), ipAddr, deviceId);
        OTPToken otp = otpService.generateOTP(cust.getId(), "EMAIL");

        log.info("MFA OTP sent for customer: {}", cust.getId());

        return LoginResponse.builder()
                .msg("MFA required")
                .sessionToken(sess.getSessionToken())
                .mfaRequired(true)
                .otpToken(otp.getToken())
                .deliveryMethod(otp.getDeliveryMethod())
                .build();
    }

    @Transactional
    public MFAVerificationResponse verifyMFA(String sessionToken, MFAVerificationRequest req) {
        log.info("Verifying MFA for session: {}", sessionToken);

        Optional<Session> optSess = sessService.getActiveSession(sessionToken);
        if (optSess.isEmpty()) {
            throw new RuntimeException("Session not found or expired");
        }

        Session sess = optSess.get();

        boolean otpValid = otpService.verifyOTP(req.getOtpToken(), req.getOtpCode());
        if (!otpValid) {
            log.warn("MFA verification failed for session: {}", sessionToken);
            throw new RuntimeException("Invalid OTP code");
        }

        sessService.verifyMFA(sessionToken);

        if (req.getTrustDevice() && sess.getDeviceId() != null) {
            sessService.trustDevice(sess.getCustomerId(), sess.getDeviceId());
        }

        log.info("MFA verification successful for session: {}", sessionToken);

        return MFAVerificationResponse.builder()
                .msg("Authentication successful")
                .sessionToken(sessionToken)
                .expiresIn(3600L)
                .mfaVerified(true)
                .build();
    }

    @Transactional
    public void logout(String sessionToken) {
        log.info("Logging out session: {}", sessionToken);
        sessService.revokeSession(sessionToken);
    }

    private UUID registerDevice(UUID customerId, String deviceId, String deviceName, 
                               String ipAddr, String userAgent) {
        log.info("Registering device: {} for customer: {}", deviceId, customerId);

        DeviceRegistration dev = DeviceRegistration.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .deviceId(deviceId)
                .deviceName(deviceName != null ? deviceName : "Unknown Device")
                .userAgent(userAgent)
                .ipAddr(ipAddr)
                .status(DeviceRegistration.DeviceStatus.PENDING)
                .isTrusted(false)
                .build();

        devRepo.save(dev);
        return dev.getId();
    }
}
