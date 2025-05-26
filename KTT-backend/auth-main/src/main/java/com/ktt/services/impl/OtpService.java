package com.ktt.services.impl;

import com.ktt.dtos.OtpRequest;
import com.ktt.dtos.OtpResponse;
import com.ktt.dtos.OtpValidationRequest;
import com.ktt.dtos.Response;
import com.ktt.entities.Otp;
import com.ktt.repository.OtpRepository;
import com.ktt.services.EmailService;
import com.ktt.utils.AppUtils;
import com.ktt.utils.ExceptionMessages;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;

    public Response sendOtp(OtpRequest otpRequest) {
        Otp existingOtp = otpRepository.findByEmailIdAndCompanyCode(
                otpRequest.getEmailId(),
                otpRequest.getCompanyCode()
        );

        if (existingOtp != null) {
            otpRepository.delete(existingOtp);
        }

        String otp = AppUtils.generateOTP();
        log.info("Generated OTP: {}", otp);

        final String subject = "KTT (Do Not Share: OTP)";
        String content = "<p>OTP for your account is " + otp + " and will expire in next 2 minutes </p>";

        otpRepository.save(Otp.builder()
                .emailId(otpRequest.getEmailId())
                .otp(otp)
                .companyCode(otpRequest.getCompanyCode())
                .expiresAt(LocalDateTime.now().plusMinutes(2))
                .build());

        try {
            emailService.sendEmail(otpRequest.getEmailId(), subject, content);
        } catch (UnsupportedEncodingException | MessagingException e) {
            log.error("Error sending OTP email: {}", e.getMessage());
            return Response.builder()
                    .statusCode(500)
                    .responseMessage(ExceptionMessages.OTP_SEND_FAILED)
                    .build();
        }

        return Response.builder()
                .statusCode(200)
                .responseMessage(ExceptionMessages.OTP_SENT)
                .build();
    }

    public Response validateOtp(OtpValidationRequest otpValidationRequest) {
        Otp otp = otpRepository.findByEmailIdAndCompanyCode(
                otpValidationRequest.getEmailId(),
                otpValidationRequest.getCompanyCode()
        );

        log.info("Validating OTP for email: {}, companyCode: {}",
                otpValidationRequest.getEmailId(),
                otpValidationRequest.getCompanyCode());

        if (otp == null) {
            return Response.builder()
                    .statusCode(400)
                    .responseMessage(ExceptionMessages.OTP_NOT_SENT)
                    .build();
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Response.builder()
                    .statusCode(400)
                    .responseMessage(ExceptionMessages.OTP_EXPIRED)
                    .build();
        }

        if (!otp.getOtp().equals(otpValidationRequest.getOtp())) {
            return Response.builder()
                    .statusCode(400)
                    .responseMessage(ExceptionMessages.OTP_INVALID)
                    .build();
        }

        return Response.builder()
                .statusCode(200)
                .responseMessage(ExceptionMessages.OTP_VERIFIED)
                .otpResponse(OtpResponse.builder()
                        .isOtpValid(true)
                        .build())
                .build();
    }


}