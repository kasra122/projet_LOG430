package com.canbankx.customer.service;

import com.canbankx.customer.domain.Session;
import com.canbankx.customer.domain.DeviceRegistration;
import com.canbankx.customer.repository.SessionRepository;
import com.canbankx.customer.repository.DeviceRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessRepo;
    private final DeviceRegistrationRepository devRepo;

    @Transactional
    public Session createSession(UUID customerId, String ipAddr, UUID deviceId) {
        log.info("Creating session for customer: {} from IP: {}", customerId, ipAddr);

        Session sess = Session.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .sessionToken(UUID.randomUUID().toString())
                .deviceId(deviceId)
                .ipAddr(ipAddr)
                .status(Session.SessionStatus.ACTIVE)
                .mfaVerified(false)
                .build();

        sessRepo.save(sess);
        log.info("Session created: {}", sess.getSessionToken());
        return sess;
    }

    @Transactional
    public void verifyMFA(String sessionToken) {
        log.info("Verifying MFA for session: {}", sessionToken);

        Optional<Session> optSess = sessRepo.findBySessionToken(sessionToken);
        if (optSess.isEmpty()) {
            throw new RuntimeException("Session not found");
        }

        Session sess = optSess.get();
        sess.setMfaVerified(true);
        sess.setLastActivity(Instant.now());
        sessRepo.save(sess);

        log.info("MFA verified for session: {}", sessionToken);
    }

    @Transactional
    public void trustDevice(UUID customerId, UUID deviceId) {
        log.info("Trusting device: {} for customer: {}", deviceId, customerId);

        Optional<DeviceRegistration> optDev = devRepo.findById(deviceId);
        if (optDev.isEmpty()) {
            throw new RuntimeException("Device not found");
        }

        DeviceRegistration dev = optDev.get();
        dev.setStatus(DeviceRegistration.DeviceStatus.TRUSTED);
        dev.setIsTrusted(true);
        devRepo.save(dev);

        log.info("Device trusted: {}", deviceId);
    }

    public Optional<Session> getActiveSession(String sessionToken) {
        Optional<Session> optSess = sessRepo.findBySessionToken(sessionToken);
        
        if (optSess.isEmpty()) {
            return Optional.empty();
        }

        Session sess = optSess.get();

        if (sess.getStatus() != Session.SessionStatus.ACTIVE) {
            return Optional.empty();
        }

        if (Instant.now().isAfter(sess.getExpiresAt())) {
            sess.setStatus(Session.SessionStatus.EXPIRED);
            sessRepo.save(sess);
            return Optional.empty();
        }

        sess.setLastActivity(Instant.now());
        sessRepo.save(sess);

        return Optional.of(sess);
    }

    @Transactional
    public void revokeSession(String sessionToken) {
        log.info("Revoking session: {}", sessionToken);

        Optional<Session> optSess = sessRepo.findBySessionToken(sessionToken);
        if (optSess.isPresent()) {
            Session sess = optSess.get();
            sess.setStatus(Session.SessionStatus.REVOKED);
            sessRepo.save(sess);
        }
    }

    public List<Session> getCustomerSessions(UUID customerId) {
        return sessRepo.findByCustomerIdAndStatus(customerId, Session.SessionStatus.ACTIVE);
    }
}
