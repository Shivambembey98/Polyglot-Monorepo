package com.ktt.controllers;

import com.ktt.dtos.Request;
import com.ktt.dtos.Response;
import com.ktt.entities.User;
import com.ktt.services.ForgotPasswordService;
import com.ktt.services.impl.AuthService;
import com.ktt.utils.ExceptionMessages;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.ktt.config.auth.TokenProvider;
import com.ktt.dtos.SignInDto;
import com.ktt.dtos.SignUpDto;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AuthService service;
    @Autowired
    private ForgotPasswordService forgotPasswordService;
    @Autowired
    private TokenProvider tokenService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody @Valid SignUpDto data)
            throws MessagingException, UnsupportedEncodingException {
        Map<String, Object> response = service.signUp(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/userChecker")
    public ResponseEntity<?> usernameChecker(@RequestBody @Valid Request data) {
        return service.loadUserByUsernameAndCompanyCode(data.login(), data.companyCode());
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signIn(@RequestBody @Valid SignInDto data) {
        String lowerCaseUserName = data.login().toLowerCase();

        if (!service.validateByUsrAndPasswordAndCmpCd(lowerCaseUserName, data.password(), data.companyCode())) {
            throw new BadCredentialsException(ExceptionMessages.INVALID_CREDENTIALS);
        }

        service.isAccountActive(lowerCaseUserName, data.companyCode());

        var usernamePassword = new UsernamePasswordAuthenticationToken(lowerCaseUserName, data.password());
        var authUser = authenticationManager.authenticate(usernamePassword);

        var accessToken = tokenService.generateAccessToken((User) authUser.getPrincipal());
        service.saveJwtToken(lowerCaseUserName, data.companyCode(), accessToken);

        Map<String, Object> response = new HashMap<>();
        response.put("message", ExceptionMessages.LOGIN_SUCCESS);
        response.put("success", true);
        response.put("username", lowerCaseUserName);
        response.put("companyCode", data.companyCode());
        response.put("accessToken", accessToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPasss(
            @RequestParam String email,
            @RequestParam String companyCode
    ) throws MessagingException, UnsupportedEncodingException {
        String token = forgotPasswordService.forgotPass(email, companyCode);

        if (token.startsWith("Invalid")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ExceptionMessages.INVALID_EMAIL));
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", ExceptionMessages.OTP_SENT);
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPass(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String otp
    ) {
        Map<String, Object> response = forgotPasswordService.resetPass(token, password, otp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<Map<String, Object>> validateOtp(
            @RequestParam String email,
            @RequestParam String companyCode,
            @RequestParam String otp
    ) {
        Map<String, Object> response = service.validateOtp(email, companyCode, otp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(
            @RequestParam String email
    ) throws MessagingException, UnsupportedEncodingException {
        // Trim and validate email
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        String trimmedEmail = email.trim();

        Map<String, Object> response = service.resendOtp(trimmedEmail);
        return ResponseEntity.ok(response);
    }
}