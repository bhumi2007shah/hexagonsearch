/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import io.litmusblox.server.model.User;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author : Arpan
 * Date : 12/10/2020
 * Class Name : LoggedInUserInfoUtil
 * Project Name : server
 */

@Log4j2
public class LoggedInUserInfoUtil {

    public static Map<String, Object> getLoggedInUserJobInformation(long jobId){
        Long startTime = System.currentTimeMillis();
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Inside getLoggedInUserInformation method. Logged in by user {}", loggedInUser.getEmail());
        Map userDetails = new HashMap(4);
        userDetails.put("userId", loggedInUser.getId());
        userDetails.put("userEmail", loggedInUser.getEmail());
        userDetails.put("userCompanyId", loggedInUser.getCompany().getId());
        userDetails.put("jobId",jobId);
        log.info("Completed adding loggedInUserInformation in {} ms", System.currentTimeMillis() - startTime);
        return userDetails;

    }

    public static Map<String, Object> getLoggedInUserInformation(){
        Long startTime = System.currentTimeMillis();
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Inside getLoggedInUserInformation method. Logged in by user {}", loggedInUser.getEmail());
        Map<String, Object> userDetails = new HashMap(3);
        userDetails.put("userId", loggedInUser.getId());
        userDetails.put("userEmail", loggedInUser.getEmail());
        userDetails.put("userCompanyId", loggedInUser.getCompany().getId());
        log.info("Completed adding loggedInUserInformation in {} ms", System.currentTimeMillis() - startTime);
        return userDetails;
    }

}
