package com.ktt.services;

import com.ktt.entities.User;
import com.ktt.repository.ForgotPasswordRepo;
import jakarta.mail.MessagingException;
import com.ktt.utils.ExceptionMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.ktt.utils.AppUtils.generateOTP;

@Service
public class ForgotPasswordService {
    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordService.class);
    private static final long EXPIRE_TOKEN = 30;

    @Autowired
    private ForgotPasswordRepo repo;
    @Autowired
    private EmailService emailService;

    //    public String forgotPass(String email){
    //        Optional<User> userOptional = Optional.ofNullable(repo.findByLogin("ankit"));
    //
    //        if(!userOptional.isPresent()){
    //            return "Invalid email id.";
    //        }
    //
    //        User user=userOptional.get();
    //        user.setToken(generateToken());
    //        user.setTokenCreationDate(LocalDateTime.now());
    //
    //        user=repo.save(user);
    //        return user.getToken();
    //    }

    public String forgotPass(String email, String companyCode) throws MessagingException, UnsupportedEncodingException {
        logger.info("Processing forgot password request for email: '{}' in company: '{}'", email, companyCode);

        Optional<User> userOptional = Optional.ofNullable(repo.findByEmailIdAndCompanyCode(email, companyCode));

        logger.debug("User retrieval result: {}", userOptional);

        if (!userOptional.isPresent()) {

            logger.warn("Forgot password request failed. Email '{}' not found in company '{}'", email, companyCode);
            return ExceptionMessages.INVALID_EMAIL;
        }

        User user = userOptional.get();
        user.setToken(generateToken());

        String otp = generateOTP();
        String content = "Your OTP is: " + otp + "\n\nPlease use this OTP to reset your password.";

        user.setTokenCreationDate(LocalDateTime.now());
        emailService.sendEmail(email, "Password Reset", content);
        logger.info("Password reset OTP sent to email '{}'", email);

        user.setOtp(otp);
        user = repo.save(user);
        logger.info("Password reset token saved for user '{}'", user.getLogin());

        return user.getToken();
    }

    public Map<String, Object> resetPass(String token, String password, String otp) {
        logger.info("Processing password reset for token: '{}'", token);
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOptional = Optional.ofNullable(repo.findByToken(token));

        logger.debug("User retrieval by token result: {}", userOptional);

        if (!userOptional.isPresent()) {
            logger.warn("Password reset failed. Invalid token: '{}'", token);
            response.put("success", false);
            response.put("message", ExceptionMessages.INVALID_TOKEN);
            return response;
        }

        User user = userOptional.get();
        LocalDateTime tokenCreationDate = user.getTokenCreationDate();

        if (isTokenExpired(tokenCreationDate)) {
            logger.warn("Password reset failed. Token expired for user '{}'", user.getLogin());
            response.put("success", false);
            response.put("message", ExceptionMessages.TOKEN_EXPIRED);
            return response;
        }

        if (!otp.equals(user.getOtp())) {
            logger.warn("Password reset failed. Invalid OTP for user '{}'", user.getLogin());
            response.put("success", false);
            response.put("message", ExceptionMessages.INVALID_OTP);
            return response;
        }

        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        boolean isValid = bcrypt.matches(password, user.getPassword()) ||
                bcrypt.matches(password, user.getPassword2()) ||
                bcrypt.matches(password, user.getPassword3());

        if (isValid) {
            logger.warn("Password reset failed. User '{}' tried to reuse an old password", user.getLogin());
            response.put("success", false);
            response.put("message", ExceptionMessages.PASSWORD_REUSE);
            return response;
        }

        // Update password history
        user.setPassword3(user.getPassword2());
        user.setPassword2(user.getPassword());
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setPasswordUpdateDate(LocalDateTime.now());
        user.setUpdateDate(LocalDateTime.now());
        user.setNumberOfAttempts(0);
        user.setAccountStatus("Active");
        user.setToken(null);
        user.setTokenCreationDate(null);

        repo.save(user);
        logger.info("Password reset successful for user '{}'", user.getLogin());

        // Prepare success response data
//        responseData.put("userId", user.getId());
//        responseData.put("updatedAt", user.getUpdateDate());

        response.put("success", true);
        response.put("message", ExceptionMessages.PASSWORD_RESET);
        response.put("data", user.getUsername());

        return response;
    }

    private String generateToken() {
        String token = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        logger.debug("Generated password reset token: {}", token);
        return token;
    }

    private boolean isTokenExpired(final LocalDateTime tokenCreationDate) {
        LocalDateTime now = LocalDateTime.now();
        Duration diff = Duration.between(tokenCreationDate, now);
        boolean expired = diff.toMinutes() >= EXPIRE_TOKEN;

        if (expired) {
            logger.warn("Token expired. Creation time: '{}'", tokenCreationDate);
        } else {
            logger.debug("Token still valid. Time elapsed: {} minutes", diff.toMinutes());
        }

        return expired;
    }

    //    public String generateOTP() {
    //        // Using SecureRandom for cryptographically strong random numbers
    //        SecureRandom secureRandom = new SecureRandom();
    //
    //        // Generate 6-digit OTP
    //        int otp = 100000 + secureRandom.nextInt(900000);
    //
    //        return String.valueOf(otp);
    //    }
}
