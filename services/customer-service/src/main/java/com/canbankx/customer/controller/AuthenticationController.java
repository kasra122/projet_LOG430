package com.canbankx.customer.controller;

import com.canbankx.customer.dto.LoginRequest;
import com.canbankx.customer.dto.LoginResponse;
import com.canbankx.customer.dto.MFAVerificationRequest;
import com.canbankx.customer.dto.MFAVerificationResponse;
import com.canbankx.customer.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authSvc;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest httpReq) {
        
        log.info("Login request for email: {}", req.getEmail());
        
        String ipAddr = getClientIp(httpReq);
        String userAgent = httpReq.getHeader("User-Agent");
        
        LoginResponse res = authSvc.login(req, ipAddr, userAgent);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<MFAVerificationResponse> verifyMFA(
            @RequestHeader("X-Session-Token") String sessionToken,
            @Valid @RequestBody MFAVerificationRequest req) {
        
        log.info("MFA verification request for session: {}", sessionToken);
        
        MFAVerificationResponse res = authSvc.verifyMFA(sessionToken, req);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("X-Session-Token") String sessionToken) {
        
        log.info("Logout request for session: {}", sessionToken);
        authSvc.logout(sessionToken);
        return ResponseEntity.ok("Logged out successfully");
    }

    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }
}
