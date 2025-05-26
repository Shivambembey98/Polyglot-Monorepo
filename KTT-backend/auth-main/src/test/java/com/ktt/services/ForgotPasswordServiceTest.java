package com.ktt.services;

import com.ktt.entities.User;
import com.ktt.repository.ForgotPasswordRepo;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceTest {

    @Mock
    private ForgotPasswordRepo repo;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ForgotPasswordService forgotPasswordService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmailId("vikas@gmail.com");
        testUser.setCompanyCode("BBT");
        testUser.setPassword(new BCryptPasswordEncoder().encode("oldPassword"));
        testUser.setToken("testToken");
        testUser.setTokenCreationDate(LocalDateTime.now());
        testUser.setOtp("123456");
    }

    @Test
    void testForgotPass_Success() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        when(repo.findByEmailIdAndCompanyCode(testUser.getEmailId(), testUser.getCompanyCode()))
                .thenReturn(testUser);
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        when(repo.save(any(User.class))).thenReturn(testUser);

        // Act
        String token = forgotPasswordService.forgotPass(testUser.getEmailId(), testUser.getCompanyCode());

        // Assert
        assertNotNull(token);
        verify(emailService, times(1)).sendEmail(eq(testUser.getEmailId()), anyString(), anyString());
        verify(repo, times(1)).save(any(User.class));
    }

    @Test
    void testForgotPass_InvalidEmail() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        when(repo.findByEmailIdAndCompanyCode("invalid@gmail.com", testUser.getCompanyCode()))
                .thenReturn(null);

        // Act
        String response = forgotPasswordService.forgotPass("invalid@gmail.com", testUser.getCompanyCode());

        // Assert
        assertEquals("Invalid email Id", response);
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void testResetPass_Success() {
        // Arrange
        when(repo.findByToken(testUser.getToken())).thenReturn(testUser);
        when(repo.save(any(User.class))).thenReturn(testUser);

        // Act
        Map<String, Object> response = forgotPasswordService.resetPass(testUser.getToken(), "newPassword", "123456");

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("Your password has been updated successfully.", response.get("message"));
        verify(repo, times(1)).save(any(User.class));
    }

    @Test
    void testResetPass_InvalidToken() {
        // Arrange
        when(repo.findByToken("invalidToken")).thenReturn(null);

        // Act
        Map<String, Object> response = forgotPasswordService.resetPass("invalidToken", "newPassword", "123456");

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("Invalid token", response.get("message"));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void testResetPass_ExpiredToken() {
        // Arrange
        testUser.setTokenCreationDate(LocalDateTime.now().minusMinutes(31)); // Expired token
        when(repo.findByToken(testUser.getToken())).thenReturn(testUser);

        // Act
        Map<String, Object> response = forgotPasswordService.resetPass(testUser.getToken(), "newPassword", "123456");

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("Token expired.", response.get("message"));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void testResetPass_InvalidOTP() {
        // Arrange
        when(repo.findByToken(testUser.getToken())).thenReturn(testUser);

        // Act
        Map<String, Object> response = forgotPasswordService.resetPass(testUser.getToken(), "newPassword", "000000");

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("Invalid OTP", response.get("message"));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void testResetPass_UsedPassword() {
        // Arrange
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        testUser.setPassword(bcrypt.encode("usedPassword"));
        when(repo.findByToken(testUser.getToken())).thenReturn(testUser);

        // Act
        Map<String, Object> response = forgotPasswordService.resetPass(testUser.getToken(), "usedPassword", "123456");

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("This password has been used before. Please try with a new one.", response.get("message"));
        verify(repo, never()).save(any(User.class));
    }
}
