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

    public static Map<String, Object> getLoggedInUserJobInformation(long jobId, int schedulerFlag){
        Long startTime = System.currentTimeMillis();
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Inside getLoggedInUserInformation method. Logged in by user {}", loggedInUser.getEmail());
        Map userDetails = getLoggedInUserInformation(schedulerFlag);
        userDetails.put("jobId",jobId);
        log.info("Completed adding loggedInUserInformation in {} ms", System.currentTimeMillis() - startTime);
        return userDetails;

    }

    public static Map<String, Object> getLoggedInUserInformation(int schedulerFlag) {
        Long startTime = System.currentTimeMillis();
        Map<String, Object> userDetails = new HashMap(3);
        String loggedInUserEmail = "admin@litmusblox.io";
        if (schedulerFlag == 0){
            User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            userDetails.put("userId", loggedInUser.getId());
            userDetails.put("userEmail", loggedInUser.getEmail());
            userDetails.put("userCompanyId", loggedInUser.getCompany().getId());
            loggedInUserEmail=loggedInUser.getEmail();
        }
        else{
            userDetails.put("userId", 0);
            userDetails.put("userEmail", loggedInUserEmail);
            userDetails.put("userCompanyId", 0);
        }
        log.info("Inside getLoggedInUserInformation method. Logged in by user {}", loggedInUserEmail);
        log.info("Completed adding loggedInUserInformation in {} ms", System.currentTimeMillis() - startTime);
        return userDetails;
    }

}
