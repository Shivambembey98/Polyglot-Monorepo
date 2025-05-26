package com.ktt.services.impl;

import com.ktt.dtos.SignUpDto;
import com.ktt.entities.User;
import com.ktt.exceptions.InvalidJwtException;
import com.ktt.repository.UserCustomRepository;
import com.ktt.repository.UserRepository;
import com.ktt.services.EmailService;
import com.ktt.utils.ExceptionMessages;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.ktt.utils.AppUtils.generateOTP;

@Service
public class AuthService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserCustomRepository userCustomRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        logger.info("Loading user by username: {}", username);
        return userRepository.findByLogin(username);
    }

    public ResponseEntity<?> loadUserByUsernameAndCompanyCode(String username, String companyCode) {
        logger.info("Checking if username '{}' exists for company '{}'", username, companyCode);
        UserDetails user = userRepository.findByLoginAndCompanyCode(username.toLowerCase(), companyCode);
        if (user != null) {
            logger.warn("Username '{}' already exists for company '{}'", username, companyCode);
            throw new InvalidJwtException(ExceptionMessages.USERNAME_EXISTS);
        }
        return ResponseEntity.ok(ExceptionMessages.USERNAME_AVAILABLE);
    }

    public ResponseEntity<?> loadUserByEmailIdAndCompanyCode(String emailId, String companyCode) {
        logger.info("Checking if email '{}' exists for company '{}'", emailId, companyCode);
        UserDetails user = userRepository.findByEmailIdAndCompanyCode(emailId.toLowerCase(), companyCode);
        if (user != null) {
            logger.warn("Email '{}' is already associated with an existing account in company '{}'", emailId, companyCode);
            throw new InvalidJwtException(ExceptionMessages.EMAIL_EXISTS);
        }
        return ResponseEntity.ok(ExceptionMessages.EMAIL_AVAILABLE);
    }

    public Boolean validateByUsrAndPasswordAndCmpCd(String login, String password, String companyCode) {
        logger.info("Validating credentials for user '{}' in company '{}'", login, companyCode);

        String encryptPass = userRepository.getPassword(login, companyCode);
        if (encryptPass == null) {
            logger.warn("Invalid login attempt for user '{}'", login);
            return false;
        }

        if ("Blocked".equals(userRepository.getAccountStatus(login, companyCode))) {
            logger.error("Blocked account login attempt: '{}'", login);
            throw new InvalidJwtException(ExceptionMessages.ACCOUNT_BLOCKED);
        }

        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        boolean isValid = bcrypt.matches(password, encryptPass);
        if (!isValid) {
            logger.warn("Invalid password attempt for '{}'", login);
            userCustomRepository.updateNoOfAttempt(login, companyCode);
            return false;
        }

        userCustomRepository.updateAccountStatus(login, companyCode);
        logger.info("Successful login for user '{}'", login);
        return true;
    }

    public void isAccountActive(String login, String companyCode) {
        logger.info("Checking account activation status for user '{}'", login);

        if (!userRepository.getMailVerificationStatus(login, companyCode)) {
            logger.warn("User '{}' has not verified their email", login);
            throw new InvalidJwtException(ExceptionMessages.EMAIL_NOT_VERIFIED);
        }

        if ("Blocked".equals(userRepository.getAccountStatus(login, companyCode))) {
            logger.error("Blocked account '{}' attempted to login", login);
            throw new InvalidJwtException(ExceptionMessages.ACCOUNT_BLOCKED);
        }
    }

    @Transactional
    public void saveJwtToken(String login, String companyCode, String token) {
        logger.info("Saving JWT token for user '{}'", login);
        userRepository.updateJwtToken(token, login, companyCode);
    }

    public Map<String, Object> signUp(SignUpDto data) throws InvalidJwtException, MessagingException, UnsupportedEncodingException {
        logger.info("Attempting user signup: '{}'", data.login());

        final String defaultAccountStatus = "Unverified";

        loadUserByUsernameAndCompanyCode(data.login(), data.companyCode());
        loadUserByEmailIdAndCompanyCode(data.emailId(), data.companyCode());

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());

        User newUser = new User(data.login().toLowerCase(), encryptedPassword, data.emailId(), data.title(),
                data.firstName(), data.middleName(), data.lastName(), data.address(), data.identification(),
                data.mobileNo(), defaultAccountStatus, data.companyCode(), data.role());

        String otp = generateOTP();
        String content = "Your OTP is: " + otp + "\n\nPlease use this OTP to verify your mail.";
        newUser.setOtp(otp);

        emailService.sendEmail(data.emailId(), "Email Verification", content);
        logger.info("OTP sent to email '{}' for verification", data.emailId());

        userRepository.save(newUser);
        logger.info("User '{}' registered successfully", newUser.getLogin());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", ExceptionMessages.USER_REGISTERED);
        response.put("username", newUser.getLogin());
        response.put("email", newUser.getEmailId());
        response.put("status", newUser.getAccountStatus());

        return response;
    }

    public Map<String, Object> validateOtp(String email, String companyCode, String otp) {
        logger.info("Validating OTP for email '{}'", email);
        User user = userRepository.findByEmailIdAndCompanyCodeAndOtp(email, companyCode, otp);
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            logger.warn("Invalid OTP attempt for email '{}'", email);
            response.put("success", false);
            response.put("message", ExceptionMessages.INVALID_OTP);
            return response;
        }

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            logger.warn("Incorrect OTP for email '{}'", email);
            response.put("success", false);
            response.put("message", ExceptionMessages.INVALID_OTP);
            return response;
        }

        user.setOtp(null);
        user.setAccountStatus("Active");
        user.setMailVerified(true);
        user.setToken(generateToken());
        user.setTokenCreationDate(null);
        userRepository.save(user);
        logger.info("OTP verified successfully for '{}'", email);

        response.put("success", true);
        response.put("token", user.getToken());
        response.put("message", ExceptionMessages.OTP_VERIFIED);
        return response;
    }

    private String generateToken() {
        return UUID.randomUUID().toString() + UUID.randomUUID().toString();
    }


    public Map<String, Object> resendOtp(String email)
            throws MessagingException, UnsupportedEncodingException {
        logger.info("Resending OTP for email '{}'", email);

        Optional<User> userOptional = userRepository.findByEmailId(email);
        if (!userOptional.isPresent()) {
            logger.warn("User not found with email '{}'", email);
            throw new InvalidJwtException(ExceptionMessages.USER_NOT_FOUND);
        }

        User user = userOptional.get();
        String newOtp = generateOTP();

        // Update user with new OTP
        user.setOtp(newOtp);
        userRepository.save(user);

        // Send email with OTP
        String content = "Your new OTP is: " + newOtp +
                "\n\nPlease use this OTP to proceed with your request.";
        emailService.sendEmail(email, "Your OTP Code", content);

        logger.info("New OTP sent to email '{}'", email);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", ExceptionMessages.OTP_RESENT);
        response.put("email", email);

        return response;
    }
}