package com.ankur.userservice.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}