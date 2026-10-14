package com.alinbudesabc.authenticator.controller;


import com.alinbudesabc.authenticator.config.CustomUserDetailsService;
import com.alinbudesabc.authenticator.model.dto.RegisteredAminDto;
import com.alinbudesabc.authenticator.model.dto.projection.UserDetailsDto;
import com.alinbudesabc.authenticator.persistance.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final CustomUserDetailsService userDetailService;
    private final CustomUserDetailsService userService;


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisteredAminDto userDto){
        userDetailService.registerUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully created: " + userDto.getUsername());
    }

    @GetMapping("/usr")
    public ResponseEntity<UserEntity> getUSer(@RequestParam(name = "username") String username){
        return ResponseEntity.ok(userService.findUserByUsername(username));
    }

    @GetMapping("users/enable")
    public ResponseEntity<?> enableAccount(@RequestParam(name = "username") String username){
        userDetailService.enablePermanentlyBlockedAccount(username);
        return ResponseEntity.ok("Account enabled");
    }

//    @PostMapping("/serviceLogin")
//    public ResponseEntity<LoginResponseDto> apiLogin(@RequestBody LoginRequestDto loginRequestDto){
//        LoginResponseDto loginResponseDto = authenticationService.authenticateService(loginRequestDto);
//        return ResponseEntity.status(loginResponseDto.status())
//                .header(AppConstants.JWT_AUTH_HEADER, loginResponseDto.jwtToken())
//                .body(loginResponseDto);
//    }

    @GetMapping("/twofactor/settings")
    public ResponseEntity<UserDetailsDto> twoFactorState(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsDto twoFactorDetails = userDetailService.getUserDetailsDto(authentication.getName());
        return ResponseEntity.ok(twoFactorDetails);
    }
}
