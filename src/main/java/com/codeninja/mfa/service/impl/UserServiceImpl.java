package com.codeninja.mfa.service.impl;

import com.codeninja.mfa.exception.InvalidTokenException;
import com.codeninja.mfa.exception.MFAServerAppException;
import com.codeninja.mfa.exception.UserAlreadyExistException;
import com.codeninja.mfa.model.entity.EmailConfirmationToken;
import com.codeninja.mfa.model.dto.MfaTokenData;
import com.codeninja.mfa.model.entity.User;
import com.codeninja.mfa.repository.EmailConfirmationTokenRepository;
import com.codeninja.mfa.repository.UserRepository;
import com.codeninja.mfa.service.EmailService;
import com.codeninja.mfa.service.TotpManager;
import com.codeninja.mfa.service.UserService;
import dev.samstevens.totp.exceptions.QrGenerationException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TotpManager totpManager;

    private final EmailService emailService;

    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;

    private static final BytesKeyGenerator DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(15);
    private static final Charset US_ASCII = Charset.forName("US-ASCII");

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TotpManager totpManager,
            EmailService emailService,
            EmailConfirmationTokenRepository emailConfirmationTokenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.totpManager = totpManager;
        this.emailService = emailService;
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
    }

    @Override
    public MfaTokenData registerUser(
            User user
    ) throws UserAlreadyExistException, QrGenerationException {
        try{
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                throw new UserAlreadyExistException("Username already exists");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setSecretKey(totpManager.generateSecretKey());
            User savedUser = userRepository.save(user);
            // Create a secure token and send email
            this.sendRegistrationConfirmationEmail(user);
            //Generate the QR Code
            String qrCode = totpManager.getQRCode(savedUser.getSecretKey());
            return MfaTokenData.builder()
                    .mfaCode(savedUser.getSecretKey())
                    .qrCode(qrCode)
                    .build();
        } catch (Exception e){
            throw new MFAServerAppException("Exception while registering the user", e);
        }
    }

    @Override
    public boolean verifyTotp(String code, String username) {
        User user = userRepository.findByUsername(username).get();
        return totpManager.verifyTotp(code, user.getSecretKey());
    }

    @Override
    public void sendRegistrationConfirmationEmail(User user) throws MessagingException {
        log.info("** sendRegistrationConfirmationEmail");
        // Generate the token
        String tokenValue = new String(Base64.encodeBase64URLSafe(DEFAULT_TOKEN_GENERATOR.generateKey()), US_ASCII);
        EmailConfirmationToken emailConfirmationToken = new EmailConfirmationToken();
        emailConfirmationToken.setToken(tokenValue);
        emailConfirmationToken.setTimeStamp(LocalDateTime.now());
        emailConfirmationToken.setUser(user);
        log.info("** sendRegistrationConfirmationEmail - emailConfirmationToken: {}", emailConfirmationToken);
        emailConfirmationTokenRepository.save(emailConfirmationToken);
        // Send email
        emailService.sendConfirmationEmail(emailConfirmationToken);
    }

    @Override
    public boolean verifyUser(String token) throws InvalidTokenException {
        EmailConfirmationToken emailConfirmationToken = emailConfirmationTokenRepository.findByToken(token);
        if (Objects.isNull(emailConfirmationToken) || !token.equals(emailConfirmationToken.getToken())) {
            throw new InvalidTokenException("Token is not valid");
        }
        User user = emailConfirmationToken.getUser();
        if (Objects.isNull(user)) {
            return false;
        }
        user.setAccountVerified(true);
        userRepository.save(user);
        emailConfirmationTokenRepository.delete(emailConfirmationToken);
        return true;
    }
}