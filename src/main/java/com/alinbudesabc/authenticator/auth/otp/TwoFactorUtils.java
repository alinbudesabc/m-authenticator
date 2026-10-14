package com.alinbudesabc.authenticator.auth.otp;

import com.warrenstrange.googleauth.GoogleAuthenticator;


//@Service
//@RequiredArgsConstructor
public class TwoFactorUtils {
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
//    private final GoogleAuthenticatorQRGenerator qrGenerator = new GoogleAuthenticatorQRGenerator();
    //TODO: user service instead repo, the repo is already injected in the CustomUserService
//    private final UserRepository userRepository;

//    @Transactional
//    public String generateAndSaveSecretKey(String username) {
//        String key = googleAuthenticator.createCredentials().getKey();
//
////        int numberOfRows = userDetailsService.save2FAKey(key, username);
//        int numberOfRows = userRepository.updateAccount(key, MessagingStrategyEnum.AUTHENTICATOR,
//                TWO_STEP_AUTH, true, username);
//        if(numberOfRows == 0){
//            throw new BadCredentialsException("could not update the account and save the 2FA key for user: " + username);
//        }
//        return key;
//    }
//
//    public boolean validateCode(String secretKey, String code) {
//        return googleAuthenticator.authorize(secretKey, Integer.parseInt(code));
//    }


//    public BufferedImage getQRCodeImage() throws WriterException, IOException {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        ImageIO.write(googleAuthenticator.getQRBarcodeImage(), "png", outputStream);
//        return ImageIO.read(outputStream.toByteArray());
//    }
}
