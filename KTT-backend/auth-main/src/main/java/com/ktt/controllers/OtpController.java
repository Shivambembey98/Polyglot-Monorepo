package com.ktt.controllers;

import com.ktt.dtos.OtpRequest;
import com.ktt.dtos.OtpValidationRequest;
import com.ktt.dtos.Response;
import com.ktt.services.EmailService;
import com.ktt.services.impl.OtpService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1/otp")
@AllArgsConstructor
public class OtpController {

    private final OtpService otpService;

    private final EmailService emailService;

    @PostMapping("/sendOtp")
    public Response sendOtp(@RequestBody OtpRequest otpRequest){
        return otpService.sendOtp(otpRequest);
    }

    @PostMapping("/validateOtp")
    public Response validateOtp(@RequestBody OtpValidationRequest otpValidationRequest){
        return otpService.validateOtp(otpValidationRequest);
    }
}
