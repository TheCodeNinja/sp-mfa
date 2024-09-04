package com.codeninja.mfa.service.impl;

import com.codeninja.mfa.service.TotpManager;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.util.Utils;
import org.springframework.stereotype.Service;

@Service
public class TotpManagerImpl implements TotpManager {

    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier myCodeVerifier;

    public TotpManagerImpl(SecretGenerator secretGenerator, QrGenerator qrGenerator, CodeVerifier myCodeVerifier) {
        this.secretGenerator = secretGenerator;
        this.qrGenerator = qrGenerator;
        this.myCodeVerifier = myCodeVerifier;
    }

    @Override
    public String generateSecretKey() {
        return secretGenerator.generate(); // 32 Byte Secret Key
    }

    @Override
    public String getQRCode(String secret) throws QrGenerationException {
        QrData qrData = new QrData.Builder()
                .label("2FA Server")
                .issuer("Youtube 2FA Demo")
                .secret(secret)
                .digits(6)
                .period(30)
                .algorithm(HashingAlgorithm.SHA512)
                .build();

        // 将生成的二维码图像数据转换为数据 URI，并返回该数据 URI 作为结果。数据 URI 是一种表示图像、文本等数据的 URL 格式
        return Utils.getDataUriForImage(qrGenerator.generate(qrData), qrGenerator.getImageMimeType());
    }

    @Override
    public boolean verifyTotp(String code, String secret) {
        return myCodeVerifier.isValidCode(secret, code);
    }
}