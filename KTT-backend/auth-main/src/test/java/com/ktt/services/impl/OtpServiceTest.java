package com.ktt.services.impl;

import com.ktt.dtos.OtpRequest;
import com.ktt.entities.Otp;
import com.ktt.repository.OtpRepository;
import com.ktt.services.EmailService;
import com.ktt.dtos.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import jakarta.mail.MessagingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OtpService otpService;

    private OtpRequest otpRequest;

    @BeforeEach
    void setUp() {
        otpRequest = new OtpRequest("test@example.com", "CompanyA");
    }

    @Test
    void testSendOtp_Success() throws MessagingException, UnsupportedEncodingException {
        // Arrange: Mock repository behavior
        when(otpRepository.findByEmailIdAndCompanyCode(otpRequest.getEmailId(), otpRequest.getCompanyCode()))
                .thenReturn(null); // No existing OTP

        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        when(otpRepository.save(any(Otp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Response response = otpService.sendOtp(otpRequest);

        // Assert
        assertEquals(200, response.getStatusCode());
        assertEquals("OTP sent to reset your password in your email", response.getResponseMessage());

        // Verify repository and email service interactions
        verify(otpRepository, times(1)).save(any(Otp.class));
        verify(emailService, times(1)).sendEmail(eq(otpRequest.getEmailId()), anyString(), anyString());
    }

    @Test
    void testSendOtp_ExistingOtpDeleted() throws MessagingException, UnsupportedEncodingException {
        // Arrange: Simulate existing OTP in DB
        Otp existingOtp = new Otp(1L, "123456", "test@example.com", "CompanyA", LocalDateTime.now(), LocalDateTime.now().plusMinutes(2));
        when(otpRepository.findByEmailIdAndCompanyCode(otpRequest.getEmailId(), otpRequest.getCompanyCode()))
                .thenReturn(existingOtp);

        doNothing().when(otpRepository).delete(existingOtp);
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        when(otpRepository.save(any(Otp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Response response = otpService.sendOtp(otpRequest);

        // Assert
        assertEquals(200, response.getStatusCode());
        assertEquals("OTP sent to reset your password in your email", response.getResponseMessage());

        // Verify delete is called before saving new OTP
        verify(otpRepository, times(1)).delete(existingOtp);
        verify(otpRepository, times(1)).save(any(Otp.class));
        verify(emailService, times(1)).sendEmail(eq(otpRequest.getEmailId()), anyString(), anyString());
    }
}
