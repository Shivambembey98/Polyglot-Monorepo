package com.ktt.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.UnsupportedEncodingException;

import static org.aspectj.bridge.MessageUtil.fail;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Manually setting the values to avoid null errors
        emailService = new EmailService();
        emailService.mailSender = mailSender;

        // Injecting test values for @Value properties
        emailService.fromEmailAddress = "noreply@test.com";
        emailService.personal = "Test Sender";
    }
    @Test
    void testSendEmail_Success() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String recipient = "test@example.com";
        String subject = "Test Subject";
        String content = "Test Content";

        // Act
        emailService.sendEmail(recipient, subject, content);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendEmail_MessagingException() throws UnsupportedEncodingException {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Simulate failure when sending email
        doAnswer(invocation -> {
            throw new MessagingException("Email sending failed");
        }).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert using try-catch
        try {
            emailService.sendEmail("test@example.com", "Subject", "Content");
        } catch (MessagingException e) {
            // Check if the exception message matches the expected message
            assertTrue(e.getMessage().contains("Email sending failed"));
        }

        // Verify method calls
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

}
