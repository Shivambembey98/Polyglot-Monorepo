package com.ktt.services.impl;

import com.ktt.entities.User;
import com.ktt.exceptions.ResourceNotFoundException;
import com.ktt.repository.UserRepository;
import com.ktt.utils.ExceptionMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    public User getProfile(String id, String email, String username) {
        if (id != null) {
            return userRepository.findById(Long.parseLong(id))
                    .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessages.USER_NOT_FOUND));
        } else if (email != null) {
            return userRepository.findByEmailId(email)
                    .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessages.USER_NOT_FOUND));
        } else if (username != null) {
            return userRepository.findUserByLogin(username)
                    .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessages.USER_NOT_FOUND));
        }
        throw new ResourceNotFoundException(ExceptionMessages.USER_NOT_FOUND);
    }

    public User updateProfile(User updatedUser) {
        User existingUser = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionMessages.USER_NOT_FOUND));

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setMiddleName(updatedUser.getMiddleName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setMobileNo(updatedUser.getMobileNo());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setCity(updatedUser.getCity());
        existingUser.setState(updatedUser.getState());
        existingUser.setNationality(updatedUser.getNationality());
        existingUser.setPinCode(updatedUser.getPinCode());
        existingUser.setGender(updatedUser.getGender());
        existingUser.setDob(updatedUser.getDob());

        return userRepository.save(existingUser);
    }

    public User softDeleteProfile(String identifier) {
        User user = getProfile(null, identifier, identifier); // Try by email or username

        if (user.isDeleted() == null) {
            user.setDeleted(false);
        }
        user.setDeleted(true);
        return userRepository.save(user);
    }
}