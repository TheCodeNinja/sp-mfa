package com.codeninja.mfa.security;

import org.springframework.security.core.Authentication;

import java.text.ParseException;

public interface JwtService {
    String generateJwt(String username) throws ParseException;
    Authentication validateJwt(String jwt);
}