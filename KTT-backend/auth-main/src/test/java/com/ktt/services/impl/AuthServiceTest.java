package com.ktt.services.impl;

import com.ktt.dtos.SignUpDto;
import com.ktt.entities.User;
import com.ktt.enums.UserRole;
import com.ktt.exceptions.InvalidJwtException;
import com.ktt.repository.UserCustomRepository;
import com.ktt.repository.UserRepository;
import com.ktt.services.EmailService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;



import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;


    @Mock
    private UserCustomRepository userCustomRepository;

    @Spy
    @InjectMocks
    private AuthService authService;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    void testLoadUserByUsername() {
        // Arrange
        String username = "Vikas";
        User mockUser = new User();
        mockUser.setLogin(username);

        when(userRepository.findByLogin(username)).thenReturn(mockUser);

        // Act
        UserDetails result = authService.loadUserByUsername(username);

        // Assert
        assertEquals(username, result.getUsername());
        verify(userRepository, times(1)).findByLogin(username); // Ensure repository method is called once
    }

    @Test
    void testLoadUserByUsernameAndCompanyCode_UserExists() {
        // Arrange
        String username = "Vikas";
        String companyCode = "Bellblaze";
        UserDetails mockUser = mock(UserDetails.class);

        when(userRepository.findByLoginAndCompanyCode(username.toLowerCase(), companyCode))
                .thenReturn(mockUser);

        // Act & Assert
        InvalidJwtException exception = assertThrows(InvalidJwtException.class,
                () -> authService.loadUserByUsernameAndCompanyCode(username, companyCode));

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, times(1)).findByLoginAndCompanyCode(username.toLowerCase(), companyCode);
    }

    @Test
    void testLoadUserByUsernameAndCompanyCode_UserNotExists() {
        // Arrange
        String username = "Vikas";
        String companyCode = "Bellblaze";

        when(userRepository.findByLoginAndCompanyCode(username.toLowerCase(), companyCode))
                .thenReturn(null);

        // Act
        ResponseEntity<?> response = authService.loadUserByUsernameAndCompanyCode(username, companyCode);

        // Assert
        assertEquals(ResponseEntity.ok("Username Available"), response);
        verify(userRepository, times(1)).findByLoginAndCompanyCode(username.toLowerCase(), companyCode);
    }

    @Test
    void testLoadUserByEmailIdAndCompanyCode_UserExists() {
        // Arrange
        String emailId = "vikas@gmail.com";
        String companyCode = "BBt";
        UserDetails mockUser = mock(UserDetails.class);

        when(userRepository.findByEmailIdAndCompanyCode(emailId.toLowerCase(), companyCode))
                .thenReturn(mockUser);

        // Act & Assert
        InvalidJwtException exception = assertThrows(InvalidJwtException.class,
                () -> authService.loadUserByEmailIdAndCompanyCode(emailId, companyCode));

        assertEquals("User already exists with this email. One EmailId can only be associated with one account.", exception.getMessage());
        verify(userRepository, times(1)).findByEmailIdAndCompanyCode(emailId.toLowerCase(), companyCode);
    }

    @Test
    void testLoadUserByEmailIdAndCompanyCode_UserNotExists() {
        // Arrange
        String emailId = "newuser@gmail.com";
        String companyCode = "BBT";

        when(userRepository.findByEmailIdAndCompanyCode(emailId.toLowerCase(), companyCode))
                .thenReturn(null);

        // Act
        ResponseEntity<?> response = authService.loadUserByEmailIdAndCompanyCode(emailId, companyCode);

        // Assert
        assertEquals(ResponseEntity.ok("EmailId is Available"), response);
        verify(userRepository, times(1)).findByEmailIdAndCompanyCode(emailId.toLowerCase(), companyCode);
    }

    @Test
    void testValidateByUsrAndPasswordAndCmpCd_PasswordNull() {
        // Arrange
        String login = "Vikas";
        String password = "Vikas123";
        String companyCode = "BBT";

        when(userRepository.getPassword(login, companyCode)).thenReturn(null);

        // Act
        boolean result = authService.validateByUsrAndPasswordAndCmpCd(login, password, companyCode);

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).getPassword(login, companyCode);
    }

    @Test
    void testValidateByUsrAndPasswordAndCmpCd_AccountBlocked() {
        // Arrange
        String login = "blockeduser";
        String password = "Vikas123";
        String companyCode = "BBT";

        when(userRepository.getPassword(login, companyCode)).thenReturn("someEncryptedPassword");
        when(userRepository.getAccountStatus(login, companyCode)).thenReturn("Blocked");

        // Act & Assert
        InvalidJwtException exception = assertThrows(InvalidJwtException.class,
                () -> authService.validateByUsrAndPasswordAndCmpCd(login, password, companyCode));

        assertEquals("Your account is blocked due to multiple unsuccessful attempts. Please contact admin", exception.getMessage());
        verify(userRepository, times(1)).getPassword(login, companyCode);
        verify(userRepository, times(1)).getAccountStatus(login, companyCode);
    }

    @Test
    void testValidateByUsrAndPasswordAndCmpCd_IncorrectPassword() {
        // Arrange
        String login = "Vikas";
        String password = "vikas1235";
        String companyCode = "BBt";
        String encryptedPassword = new BCryptPasswordEncoder().encode("correctpassword");

        when(userRepository.getPassword(login, companyCode)).thenReturn(encryptedPassword);
        when(userRepository.getAccountStatus(login, companyCode)).thenReturn("Active");

        // Act
        boolean result = authService.validateByUsrAndPasswordAndCmpCd(login, password, companyCode);

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).getPassword(login, companyCode);
        verify(userRepository, times(1)).getAccountStatus(login, companyCode);
        verify(userCustomRepository, times(1)).updateNoOfAttempt(login, companyCode);
    }

    @Test
    void testValidateByUsrAndPasswordAndCmpCd_CorrectPassword() {
        // Arrange
        String login = "Vikas";
        String password = "Vikas123";
        String companyCode = "BBT";
        String encryptedPassword = new BCryptPasswordEncoder().encode(password);

        when(userRepository.getPassword(login, companyCode)).thenReturn(encryptedPassword);
        when(userRepository.getAccountStatus(login, companyCode)).thenReturn("Active");

        // Act
        boolean result = authService.validateByUsrAndPasswordAndCmpCd(login, password, companyCode);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).getPassword(login, companyCode);
        verify(userRepository, times(1)).getAccountStatus(login, companyCode);
        verify(userCustomRepository, times(1)).updateAccountStatus(login, companyCode);
    }

    @Test
    void testIsAccountActive_MailNotVerified() {
        // Arrange
        String login = "testuser";
        String companyCode = "ABC123";

        when(userRepository.getMailVerificationStatus(login, companyCode)).thenReturn(false);

        // Act & Assert
        InvalidJwtException exception = assertThrows(InvalidJwtException.class,
                () -> authService.isAccountActive(login, companyCode));

        assertEquals("Mail is not yet verified. Please verify to login", exception.getMessage());
        verify(userRepository, times(1)).getMailVerificationStatus(login, companyCode);
    }

    @Test
    void testIsAccountActive_AccountBlocked() {
        // Arrange
        String login = "testuser";
        String companyCode = "ABC123";

        when(userRepository.getMailVerificationStatus(login, companyCode)).thenReturn(true);
        when(userRepository.getAccountStatus(login, companyCode)).thenReturn("Blocked");

        // Act & Assert
        InvalidJwtException exception = assertThrows(InvalidJwtException.class,
                () -> authService.isAccountActive(login, companyCode));

        assertEquals("Your account is blocked due to multiple unsuccessful attempts. Please contact admin", exception.getMessage());
        verify(userRepository, times(1)).getMailVerificationStatus(login, companyCode);
        verify(userRepository, times(1)).getAccountStatus(login, companyCode);
    }

    @Test
    void testIsAccountActive_AccountActive() {
        // Arrange
        String login = "testuser";
        String companyCode = "ABC123";

        when(userRepository.getMailVerificationStatus(login, companyCode)).thenReturn(true);
        when(userRepository.getAccountStatus(login, companyCode)).thenReturn("Active");

        // Act & Assert
        assertDoesNotThrow(() -> authService.isAccountActive(login, companyCode));

        verify(userRepository, times(1)).getMailVerificationStatus(login, companyCode);
        verify(userRepository, times(1)).getAccountStatus(login, companyCode);
    }

    @Test
    void testSaveJwtToken() {
        // Arrange
        String login = "testuser";
        String companyCode = "ABC123";
        String token = "sample.jwt.token";

        // Act
        authService.saveJwtToken(login, companyCode, token);

        // Assert
        verify(userRepository, times(1)).updateJwtToken(token, login, companyCode);
    }

    @Test
    void testSignUp_UsernameAlreadyExists() {
        // Arrange
        SignUpDto signUpDto = new SignUpDto("testuser", "password", "test@example.com", "Title",
                "First", "Middle", "Last", "Address", "ID123", new byte[]{}, "1234567890", "CompanyA", UserRole.USER);

        doThrow(new InvalidJwtException("Username already exists"))
                .when(authService) // Now possible because @Spy allows partial mocking
                .loadUserByUsernameAndCompanyCode(signUpDto.login(), signUpDto.companyCode());

        // Act & Assert
        InvalidJwtException exception = assertThrows(InvalidJwtException.class,
                () -> authService.signUp(signUpDto));

        assertEquals("Username already exists", exception.getMessage());
        verify(authService, times(1)).loadUserByUsernameAndCompanyCode(signUpDto.login(), signUpDto.companyCode());
    }

    @Test
    void testSignUp_EmailAlreadyExists() {
        // Arrange
        SignUpDto signUpDto = new SignUpDto("testuser", "password", "test@example.com", "Title",
                "First", "Middle", "Last", "Address", "ID123", new byte[]{}, "1234567890", "CompanyA", UserRole.USER);

        when(userRepository.findByLoginAndCompanyCode(signUpDto.login(), signUpDto.companyCode()))
                .thenReturn(null); // No user found, so username is available.

        when(userRepository.findByEmailIdAndCompanyCode(signUpDto.emailId(), signUpDto.companyCode()))
                .thenReturn(new User()); // Simulating email already exists.

        // Act & Assert
        InvalidJwtException exception = assertThrows(InvalidJwtException.class,
                () -> authService.signUp(signUpDto));

        assertEquals("User already exists with this email. One EmailId can only be associated with one account.",
                exception.getMessage());

        verify(userRepository, times(1)).findByLoginAndCompanyCode(signUpDto.login(), signUpDto.companyCode());
        verify(userRepository, times(1)).findByEmailIdAndCompanyCode(signUpDto.emailId(), signUpDto.companyCode());
    }


    @Test
    void testSignUp_Success() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        SignUpDto signUpDto = new SignUpDto("testuser", "password", "test@example.com", "Title",
                "First", "Middle", "Last", "Address", "ID123", new byte[]{}, "1234567890", "CompanyA", UserRole.USER);

        // Mock: Username and email do not exist
        when(userRepository.findByLoginAndCompanyCode(signUpDto.login(), signUpDto.companyCode()))
                .thenReturn(null);
        when(userRepository.findByEmailIdAndCompanyCode(signUpDto.emailId(), signUpDto.companyCode()))
                .thenReturn(null);

        // Mock: Save user
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        // Mock: Email sending
        doNothing().when(emailService).sendEmail(eq(signUpDto.emailId()), anyString(), anyString());

        // Act
        Map<String, Object> response = authService.signUp(signUpDto);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("User registered successfully. Please verify your email.", response.get("message"));
        assertEquals(signUpDto.login(), response.get("username"));
        assertEquals(signUpDto.emailId(), response.get("email"));
        assertEquals("Unverified", response.get("status"));

        // Verify interactions
        verify(userRepository, times(1)).findByLoginAndCompanyCode(signUpDto.login(), signUpDto.companyCode());
        verify(userRepository, times(1)).findByEmailIdAndCompanyCode(signUpDto.emailId(), signUpDto.companyCode());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendEmail(eq(signUpDto.emailId()), anyString(), anyString());
    }

    @Test
    void testValidateOtp_Success() {
        // Arrange
        String email = "test@example.com";
        String companyCode = "CompanyA";
        String otp = "123456";
        User mockUser = new User();
        mockUser.setOtp(otp);

        when(userRepository.findByEmailIdAndCompanyCodeAndOtp(email, companyCode, otp)).thenReturn(mockUser);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        Map<String, Object> response = authService.validateOtp(email, companyCode, otp);

        // Assert
        assertTrue((Boolean) response.get("success"));
        assertEquals("Otp is verified", response.get("message"));
        assertNotNull(response.get("token"));

        // Verify that save was called once
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void testValidateOtp_InvalidOtp() {
        // Arrange
        String email = "test@example.com";
        String companyCode = "CompanyA";
        String otp = "123456";

        when(userRepository.findByEmailIdAndCompanyCodeAndOtp(email, companyCode, otp)).thenReturn(null);

        // Act
        Map<String, Object> response = authService.validateOtp(email, companyCode, otp);

        // Assert
        assertFalse((Boolean) response.get("success"));
        assertEquals("Invalid OTP", response.get("message"));

        // Verify save() was never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGenerateToken() throws Exception {
        AuthService authService = new AuthService();
        Method method = AuthService.class.getDeclaredMethod("generateToken");
        method.setAccessible(true);

        String token = (String) method.invoke(authService);

        assertNotNull(token);
        assertEquals(72, token.length()); // ✅ Corrected expected length
        assertTrue(token.contains("-"));  // ✅ Ensure it contains hyphens
    }


}
