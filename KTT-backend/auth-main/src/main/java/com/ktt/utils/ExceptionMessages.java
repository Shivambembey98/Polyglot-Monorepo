package com.ktt.utils;

public class ExceptionMessages {
    // Auth related messages
    public static final String INVALID_CREDENTIALS = "Invalid Credentials";
    public static final String ACCOUNT_BLOCKED = "Your account is blocked due to multiple unsuccessful attempts. Please contact admin";
    public static final String EMAIL_NOT_VERIFIED = "Mail is not yet verified. Please verify to login";
    public static final String USERNAME_EXISTS = "Username already exists";
    public static final String EMAIL_EXISTS = "User already exists with this email. One EmailId can only be associated with one account.";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String TOKEN_EXPIRED = "Token expired.";
    public static final String INVALID_OTP = "Invalid OTP";
    public static final String PASSWORD_REUSE = "This password has been used before. Please try with a new one.";
    public static final String INVALID_EMAIL = "Invalid email Id";

    // OTP related messages
    public static final String PASSWORD_RESET_EMAIL_CONTENT = "Your OTP is: %s\n\nPlease use this OTP to reset your password.";
    public static final String OTP_NOT_SENT = "You have not sent an Otp";
    public static final String OTP_EXPIRED = "Otp has expired";
    public static final String OTP_INVALID = "Otp is Invalid";
    public static final String OTP_VERIFIED = "Otp is verified";
    public static final String OTP_SEND_FAILED = "Failed to send OTP email";

    // Success messages
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String USER_REGISTERED = "User registered successfully. Please verify your email.";
    public static final String PASSWORD_RESET = "Your password has been updated successfully.";
    public static final String OTP_SENT = "OTP sent to reset your password in your email";
    public static final String USERNAME_AVAILABLE = "Username Available";
    public static final String EMAIL_AVAILABLE = "EmailId is Available";

    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_UPDATED_SUCCESSFULLY = "User profile updated successfully";
    public static final String USER_DELETED_SUCCESSFULLY = "User profile deleted successfully";
    public static final String EMAIL_NOT_FOUND = "No user found with this email";
    public static final String USERNAME_NOT_FOUND = "No user found with this username";
    public static final String USER_FETCHED_SUCCESSFULLY = "User profile fetched successfully";
    public static final String PROFILE_FETCH_SUCCESS = "Success";
    public static final String PROFILE_UPDATE_SUCCESS = "Success";
    public static final String PROFILE_DELETE_SUCCESS = "Success";


    //public static final String OTP_RESENT = "OTP has been resent successfully";

    //   public static final String USER_NOT_FOUND = "User not found with this email";
    public static final String ACCOUNT_ALREADY_ACTIVE = "Email is already verified";
    public static final String OTP_RESENT = "New OTP has been sent to your email";

    public static final String ACTIVE_USERS_FETCHED = "Active users fetched successfully";
    public static final String DELETED_USERS_FETCHED = "Deleted users fetched successfully";
    public static final String ALL_USERS_FETCHED = "All users fetched successfully";
}