/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.error;

import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.utils.SentryUtil;
import lombok.extern.log4j.Log4j2;
import org.hibernate.exception.DataException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.RollbackException;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception handler to send the correct error code, http status and error message from the Rest API to the caller
 *
 * @author : Shital Raval
 * Date : 6/8/19
 * Time : 11:24 AM
 * Class Name : GlobalControllerExceptionHandler
 * Project Name : server
 */
@Log4j2
@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    private final String templateStr = "messageTemplate='";

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
        //System.out.println(ex);
        Map<String, String> breadCrumb = new HashMap<>();
        breadCrumb.put("ExceptionType","WebException");
        if(null != ex.getUserId())
            breadCrumb.put("userId", ex.getUserId().toString());

        if(ex.getBreadCrumb()!=null) {
            for (String key : ex.getBreadCrumb().keySet()) {
                breadCrumb.put(key, ex.getBreadCrumb().get(key));
            }
        }
        SentryUtil.logWithStaticAPI(ex.getUserEmail(), ex.getErrorMessage(), breadCrumb);
        return new ApiErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getErrorMessage());
    }

    @ExceptionHandler(value = { ConstraintViolationException.class })
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiErrorResponse handleConstraintViolation(ConstraintViolationException exception) {
        int index = exception.getMessage().indexOf(templateStr);
        Map<String, String> breadCrumb = new HashMap<>();
        breadCrumb.put("ExceptionType","ConstraintViolationException");
        if(null != exception.getCause() && index != -1){
            SentryUtil.logWithStaticAPI(null, exception.getCause().getMessage().substring(index+templateStr.length(),exception.getCause().getMessage().lastIndexOf('}')-1), breadCrumb);
            return new ApiErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(),exception.getMessage().substring(index+templateStr.length(),exception.getMessage().lastIndexOf('}')-1));
        }
        SentryUtil.logWithStaticAPI(null, (exception.getCause()!=null)?exception.getCause().getMessage():exception.getMessage(), breadCrumb);
        return new ApiErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getMessage());
    }

    @ExceptionHandler(value = { RollbackException.class })
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiErrorResponse handleRollback(RollbackException exception) {
        int index = exception.getCause().getMessage().indexOf(templateStr);
        Map<String, String> breadCrumb = new HashMap<>();
        breadCrumb.put("ExceptionType","RollbackException");
        if (null != exception.getCause() && index != -1) {
            SentryUtil.logWithStaticAPI(null, exception.getCause().getMessage().substring(index+templateStr.length(),exception.getCause().getMessage().lastIndexOf('}')-1), breadCrumb);
            return new ApiErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(),exception.getCause().getMessage().substring(index+templateStr.length(),exception.getCause().getMessage().lastIndexOf('}')-1));
        }
        SentryUtil.logWithStaticAPI(null, (exception.getCause()!=null)?exception.getCause().getMessage():exception.getMessage(), breadCrumb);
        return new ApiErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getCause().getMessage());
    }

    @ExceptionHandler(value = { UnexpectedRollbackException.class })
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiErrorResponse handleUnexpectedRollback(UnexpectedRollbackException exception) {
        Map<String, String> breadCrumb = new HashMap<>();
        breadCrumb.put("ExceptionType","RollbackException");
        SentryUtil.logWithStaticAPI(null, exception.getLocalizedMessage(), breadCrumb);
        return new ApiErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getLocalizedMessage());
    }

    @ExceptionHandler(value = { DataException.class })
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiErrorResponse handleDataException(DataException exception) {
        Map<String, String> breadCrumb = new HashMap<>();
        breadCrumb.put("ExceptionType","DataException");
        SentryUtil.logWithStaticAPI(null, exception.getLocalizedMessage(), breadCrumb);
        return new ApiErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getLocalizedMessage());
    }
}
