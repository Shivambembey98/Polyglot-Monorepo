package com.ktt.repository;

//import org.springframework.http.HttpStatus;

public final class AppConstants {


    private AppConstants() {
    }

    public static final class Tenant {
        public static final String USERNAME_OR_EMAIL_EXISTS = "Username or email already exists";
        public static final String USERNAME_EXISTS = "Username already exists";
        public static final String DEFAULT_CREATED_BY = "SYSTEM";
        public static final String INVALID_EMAIL = "Email is Incorrect ";
        public static final String INVALID_PASSWORD = "Password is Incorrect";
    }

    public static final class Validation {
        public static final String TENANT_NAME_REQUIRED = "Tenant name is required";
        public static final String USERNAME_REQUIRED = "Username is required";
        public static final String USERNAME_LENGTH = "Username must be between {min} and {max} characters";
        public static final String PASSWORD_REQUIRED = "Password is required";
        public static final String PASSWORD_LENGTH = "Password must be at least {min} characters";
        public static final String EMAIL_REQUIRED = "Email is required";
        public static final String EMAIL_INVALID = "Email should be valid";
        public static final String MOBILE_INVALID = "Mobile number should be 10 digits";
    }

    public static final class Status {
        public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

        // Add other status messages if needed
        private Status() {
        }
    }
}