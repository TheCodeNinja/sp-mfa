package com.codeninja.mfa.repository;

import com.codeninja.mfa.model.entity.EmailConfirmationToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailConfirmationTokenRepository extends MongoRepository<EmailConfirmationToken, String> {
    EmailConfirmationToken findByToken(String token);
}