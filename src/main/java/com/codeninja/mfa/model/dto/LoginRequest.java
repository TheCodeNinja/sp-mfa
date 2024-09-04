package com.codeninja.mfa.model.dto;

import lombok.Data;

@Data
public class LoginRequest {
    String username;
    String password;
}