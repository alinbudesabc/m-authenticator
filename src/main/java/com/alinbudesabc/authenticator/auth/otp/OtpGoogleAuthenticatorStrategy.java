package com.alinbudesabc.authenticator.auth.otp;

import com.alinbudesabc.authenticator.config.CustomUserDetailsService;
import com.alinbudesabc.authenticator.enums.AuthTypeEnum;
import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import com.alinbudesabc.authenticator.model.dto.TwoFactorDto;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component("authenticator")
@RequiredArgsConstructor
public class OtpGoogleAuthenticatorStrategy implements OtpMessagingStrategy{

    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    private final CustomUserDetailsService userDetailsService;


    @Override
    public MessagingStrategyEnum getType() {
        return MessagingStrategyEnum.AUTHENTICATOR;
    }


    @Override
    public boolean validateCode(String secretKey, String code) {
        return googleAuthenticator.authorize(secretKey, Integer.parseInt(code));
    }


    // Generate a QR code image in Base64 format
    public byte[] generateQRImageAndUpdateAccount(String username) {
        String secretKey = googleAuthenticator.createCredentials().getKey();
        userDetailsService.updateAccountWith(
                TwoFactorDto.builder()
                        .username(username)
                        .secretTfaKey(secretKey)
                        .twoFactorEnabled(true)
                        .authTypeEnum(AuthTypeEnum.TWO_STEP_AUTH)
                        .messagingStrategyEnum(MessagingStrategyEnum.AUTHENTICATOR)
                        .build()
        );

        String issuer = "AppointmentApp";
        String qrCodeText = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, username, secretKey, issuer
        );
        try {
            Map<EncodeHintType, Object> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = new QRCodeWriter().encode(qrCodeText, BarcodeFormat.QR_CODE, 200, 200, hintMap);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return Base64.getEncoder().encode(outputStream.toByteArray());

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
