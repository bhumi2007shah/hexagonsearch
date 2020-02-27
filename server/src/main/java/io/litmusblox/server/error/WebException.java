/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.error;

import io.litmusblox.server.model.User;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.Map;

/**
 * @author : Sumit
 * Date : 19/7/19
 * Time : 12:32 PM
 * Class Name : WebException
 * Project Name : server
 */
@Data
public class WebException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 2980596172764187994L;
    private String errorMessage;
    private HttpStatus errorCode;
    private String userEmail;
    private Long userId;
    private Map<String, String> breadCrumb;

    public WebException(String errorMessage, HttpStatus errorCode)
    {
        super();
        if(!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")){
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            this.userId = user.getId();
            this.userEmail = user.getEmail();
        }
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public WebException(String message, Throwable cause)
    {
        super(message, cause);

    }

    public WebException(String errorMessage, HttpStatus errorCode, Map<String, String> breadCrumb)
    {
        super();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        this.userId = user.getId();
        this.userEmail = user.getEmail();
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.breadCrumb = breadCrumb;
    }

    public WebException(String errorMessage, HttpStatus errorCode, Throwable cause)
    {
        super(cause);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        this.userId = user.getId();
        this.userEmail = user.getEmail();
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }
    public WebException(String errorMessage, HttpStatus errorCode, Map<String, String> breadCrumb, Throwable cause)
    {
        super(cause);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        this.userId = user.getId();
        this.userEmail = user.getEmail();
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.breadCrumb = breadCrumb;
    }
}
