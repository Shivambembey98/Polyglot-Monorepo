package com.ktt.controllers;

import com.ktt.dtos.ApiResponse;
import com.ktt.dtos.UserProfileUpdateDTO;
import com.ktt.entities.User;
import com.ktt.exceptions.ResourceNotFoundException;
import com.ktt.services.impl.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Autowired
    private ProfileService profileService;


    @GetMapping
    public ResponseEntity<ApiResponse> getProfile(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username) {

        if (id == null && email == null && username == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(null, "At least one parameter (id, email, or username) is required", null)
            );
        }

        try {
            User user = profileService.getProfile(id, email, username);
            Map<String, Object> userData = convertToResponse(user);
            return ResponseEntity.ok(
                    new ApiResponse("true", "Profile fetched successfully", userData)
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse(null, e.getMessage(), null)
            );
        }
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<ApiResponse> updateProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileUpdateDTO updatedUserDto) {
        try {
            // Get user by ID
            User currentUser = profileService.getProfile(String.valueOf(userId), null, null);

            // Update fields from DTO
            currentUser.setFirstName(updatedUserDto.getFirstName());
            currentUser.setMiddleName(updatedUserDto.getMiddleName());
            currentUser.setLastName(updatedUserDto.getLastName());
            currentUser.setMobileNo(updatedUserDto.getMobileNo());
            currentUser.setAddress(updatedUserDto.getAddress());
            currentUser.setCity(updatedUserDto.getCity());
            currentUser.setState(updatedUserDto.getState());
            currentUser.setNationality(updatedUserDto.getNationality());
            currentUser.setPinCode(updatedUserDto.getPinCode());
            currentUser.setGender(updatedUserDto.getGender());
            currentUser.setDob(updatedUserDto.getDob());

            User updatedUser = profileService.updateProfile(currentUser);
            Map<String, Object> userData = convertToResponse(updatedUser);
            return ResponseEntity.ok(
                    new ApiResponse("true", "Profile updated successfully", userData)
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse(null, e.getMessage(), null)
            );
        }
    }

    @DeleteMapping("/{identifier}")
    public ResponseEntity<ApiResponse> softDeleteProfile(@PathVariable String identifier) {
        try {
            User user = profileService.softDeleteProfile(identifier);
            Map<String, Object> userData = convertToResponse(user);
            return ResponseEntity.ok(
                    new ApiResponse("true", "Profile deleted successfully", userData)
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse(null, e.getMessage(), null)
            );
        }
    }

    private Map<String, Object> convertToResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmailId());
        response.put("username", user.getLogin());
        response.put("firstName", user.getFirstName());
        response.put("middleName", user.getMiddleName());
        response.put("lastName", user.getLastName());
        response.put("mobileNo", user.getMobileNo());
        response.put("address", user.getAddress());
        response.put("city", user.getCity());
        response.put("state", user.getState());
        response.put("nationality", user.getNationality());
        response.put("pinCode", user.getPinCode());
        response.put("gender", user.getGender());
        response.put("dob", user.getDob());
        response.put("status", user.isDeleted() ? "Deleted" : "Active");

        return response;
    }
}