package com.alinbudesabc.authenticator.errorhandler;


import com.alinbudesabc.authenticator.exception.BlockedUserException;
import com.alinbudesabc.authenticator.exception.InvalidRefreshTokenException;
import com.alinbudesabc.authenticator.exception.TemporallyBlockedAccountException;
import com.alinbudesabc.authenticator.exception.PasswordChangeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

//    private final View error;
//
//    public GlobalExceptionHandler(View error) {
//        this.error = error;
//    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialException(BadCredentialsException ex) {
        return createApiResponse(ex, HttpStatus.FORBIDDEN, 0);
    }

    @ExceptionHandler(TemporallyBlockedAccountException.class)
    public ResponseEntity<ApiErrorResponse> handleLoginCacheException(TemporallyBlockedAccountException ex) {
        return createApiResponse(ex, HttpStatus.FORBIDDEN, 0);
    }

    @ExceptionHandler(BlockedUserException.class)
    public ResponseEntity<ApiErrorResponse> handleBlockedAccountException(BlockedUserException ex){
        return createApiResponse(ex, HttpStatus.LOCKED, 0);
    }

    @ExceptionHandler(HttpServerErrorException.InternalServerError.class)
    public ResponseEntity<ApiErrorResponse> handleInternalException(HttpServerErrorException ex){
        return createApiResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, 0);
    }

    @ExceptionHandler(PasswordChangeException.class)
    public ResponseEntity<ApiErrorResponse> handlePasswordException(PasswordChangeException ex){
        return createApiResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, 0);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePasswordException(UsernameNotFoundException ex){
        return createApiResponse(ex, HttpStatus.NOT_FOUND, 0);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex){
        log.error("ERROR from backend");
        return createApiResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, 0);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleException(InvalidRefreshTokenException ex){
        return createApiResponse(ex, HttpStatus.FORBIDDEN, 9000);
    }

    private ResponseEntity<ApiErrorResponse> createApiResponse(Exception ex, HttpStatus httpStatus, int errCode) {
        ApiErrorResponse apiResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(httpStatus.value())
                .errorCode(errCode)
                .message(ex.getMessage())
                .error(ex.getMessage())
                .build();
        return new ResponseEntity<>(apiResponse, httpStatus);
    }

}
