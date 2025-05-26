package com.ktt.dtos;

import com.ktt.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private String message;
    private User user;
}
