package com.codeninja.mfa.service;

import com.codeninja.mfa.exception.InvalidTokenException;
import com.codeninja.mfa.exception.UserAlreadyExistException;
import com.codeninja.mfa.model.dto.MfaTokenData;
import com.codeninja.mfa.model.entity.User;
import dev.samstevens.totp.exceptions.QrGenerationException;
import jakarta.mail.MessagingException;

public interface UserService {
    MfaTokenData registerUser(User user) throws UserAlreadyExistException, QrGenerationException;
    //MfaTokenData mfaSetup(String email) throws UnkownIdentifierException, QrGenerationException;
    boolean verifyTotp(final String code,String username);

    void sendRegistrationConfirmationEmail(final User user) throws MessagingException;
    boolean verifyUser(final String token) throws InvalidTokenException;
}