/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.error;

import io.litmusblox.server.constant.IErrorMessages;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler to send the correct error code, http status and error message from the Rest API to the caller
 *
 * @author : Shital Raval
 * Date : 6/8/19
 * Time : 11:24 AM
 * Class Name : GlobalControllerExceptionHandler
 * Project Name : server
 */
@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(value = { BadCredentialsException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleBadCredentialsException(BadCredentialsException ex) {
        return new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(), IErrorMessages.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(value = { DisabledException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleDisabledException(DisabledException ex) {
        return new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(), IErrorMessages.DISABLED_USER);
    }

    @ExceptionHandler(value = { UsernameNotFoundException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(), IErrorMessages.USER_NOT_FOUND);
    }

    @ExceptionHandler(value = { ValidationException.class })
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiErrorResponse handleValidationException(ValidationException ex) {
        return new ApiErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getErrorMessage());
    }

    @ExceptionHandler(value = { WebException.class })
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiErrorResponse handleWebException(WebException ex) {
        System.out.println(ex);
        return new ApiErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getErrorMessage());
    }
}
