package com.codeninja.mfa.controller;

import com.codeninja.mfa.exception.InvalidTokenException;
import com.codeninja.mfa.model.dto.LoginRequest;
import com.codeninja.mfa.model.dto.MfaVerificationRequest;
import com.codeninja.mfa.model.dto.MfaVerificationResponse;
import com.codeninja.mfa.model.entity.User;
import com.codeninja.mfa.security.JwtService;
import com.codeninja.mfa.service.UserService;
import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@CrossOrigin
@RestController
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationProvider authenticationProvider;

    public AuthController(
            UserService userService,
            JwtService jwtService,
            AuthenticationProvider authenticationProvider
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationProvider = authenticationProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Validated @RequestBody User user
    ) {
        log.info("register - payload: {}", user);
        // Register User // Generate QR code using the Secret KEY
        try {
            return ResponseEntity.ok(userService.registerUser(user));
        } catch (QrGenerationException e) {
            return ResponseEntity.internalServerError().body("Something went wrong. Try again.");
        }
    }

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<?> login(
            @Validated @RequestBody LoginRequest loginRequest
    ) {
        // Validate the user credentials and return the JWT / send redirect to MFA page
        try {// Get the user and Compare the password
            Authentication authentication = authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok(MfaVerificationResponse.builder()
                    .username(loginRequest.getUsername())
                    .tokenValid(Boolean.FALSE)
                    .authValid(Boolean.TRUE)
                    .mfaRequired(Boolean.TRUE)
                    .message("User Authenticated using username and Password")
                    .jwt("")
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(MfaVerificationResponse.builder()
                    .username(loginRequest.getUsername())
                    .tokenValid(Boolean.FALSE)
                    .authValid(Boolean.FALSE)
                    .mfaRequired(Boolean.FALSE)
                    .message("Invalid Credentials. Please try again.")
                    .jwt("")
                    .build());
        }
    }

    @PostMapping("/verifyTotp")
    public ResponseEntity<?> verifyTotp(
            @Validated @RequestBody MfaVerificationRequest request
    ) throws ParseException {
        MfaVerificationResponse mfaVerificationResponse = MfaVerificationResponse.builder()
                .username(request.getUsername())
                .tokenValid(Boolean.FALSE)
                .message("Token is not Valid. Please try again.")
                .build();

        // Validate the OTP
        if(userService.verifyTotp(request.getTotp(), request.getUsername())){
            //GENERATE JWT
            mfaVerificationResponse = MfaVerificationResponse.builder()
                    .username(request.getUsername())
                    .tokenValid(Boolean.TRUE)
                    .message("Token is valid")
                    .jwt(jwtService.generateJwt(request.getUsername()))
                    .build();
        }
        return ResponseEntity.ok(mfaVerificationResponse);
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(
            @RequestParam("token") String token
    ) throws InvalidTokenException {
        try {
            if (userService.verifyUser(token)) {
                return ResponseEntity.ok("Your email has been successfully verified.");
            } else {
                return ResponseEntity.ok("User details not found. Please login and regenerate the confirmation link.");
            }
        } catch (InvalidTokenException e) {
            return ResponseEntity.ok("Link expired or token already verified.");
        }
    }
}