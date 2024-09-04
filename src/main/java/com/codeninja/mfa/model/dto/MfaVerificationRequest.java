package com.codeninja.mfa.model.dto;

import lombok.Data;

@Data
public class MfaVerificationRequest {
    private String username;
    private String totp;
}