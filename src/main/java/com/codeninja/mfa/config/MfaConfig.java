package com.codeninja.mfa.config;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MfaConfig {
    @Bean
    public SecretGenerator secretGenerator(){
        return new DefaultSecretGenerator();
    }

    @Bean
    public QrGenerator qrGenerator(){
        return new ZxingPngQrGenerator();
    }

    @Bean
    public CodeVerifier myCodeVerifier() {
        // Create a time provider using the system time
        TimeProvider timeProvider = new SystemTimeProvider();

        // Create a code generator using the SHA512 hashing algorithm and code length of 6
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA512, 6);

        // Create a code verifier with the code generator and time provider
        DefaultCodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        codeVerifier.setTimePeriod(30); // Set the time period for which the codes are valid (in seconds)
        // 这意味着代码验证器将在当前时间段以及之前的两个时间段内认为代码是有效的。
        // 因此，客户端可以使用在过去90秒（30秒 * 3）内生成的代码进行身份验证。
        codeVerifier.setAllowedTimePeriodDiscrepancy(2); // Set the allowed time period discrepancy (in time periods) to account for clock differences
        return codeVerifier;
    }
}