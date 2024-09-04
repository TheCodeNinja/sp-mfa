package com.codeninja.mfa.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

@Service
public class JwtServiceImpl implements JwtService {

    private final String key = "jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));

    @Override
    public String generateJwt(String username) throws ParseException {
        Date date= new Date();
        return  Jwts.builder()
                .setIssuer("MFA Server")
                .setSubject("JWT Auth Token")
                .claim("username", username)
                .setIssuedAt(date)
                .setExpiration(new Date(date.getTime() + 60000))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public Authentication validateJwt(String jwt) {
        // Create a JWT parser with the provided secret key
        JwtParser jwtParser = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build();
        // Parse the JWT and retrieve the claims
        Claims claims = jwtParser.parseClaimsJws(jwt).getBody();
        // Retrieve the "username" claim from the JWT
        String username = (String)claims.getOrDefault("username",null);
        if (Objects.nonNull(username)) {
            // Create and return an authenticated UsernamePasswordAuthenticationToken
            // with the username and an empty list of authorities
            return new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
        }
        // Return null if the "username" claim is missing or not valid
        return null;
    }
}
