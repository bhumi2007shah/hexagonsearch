/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.User;
import io.litmusblox.server.service.LoginResponseBean;
import io.litmusblox.server.service.impl.LbUserDetailsService;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Controller class for all authentication related apis like:
 * 1. Login
 * 2. Reset password
 * 3. Forgot password
 * 4. Activate user
 *
 * @author : Shital Raval
 * Date : 18/7/19
 * Time : 9:59 AM
 * Class Name : AuthController
 * Project Name : server
 */
@CrossOrigin(origins = "*", methods = {RequestMethod.PUT,RequestMethod.POST,RequestMethod.OPTIONS}, allowedHeaders = {"Content-Type", "Authorization","X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"}, exposedHeaders = {"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"})
@RestController
@RequestMapping("/api/auth")
@Log4j2
public class AuthController {

    @Autowired
    LbUserDetailsService userDetailsService;

    /**
     * Api to handle login request
     * @param user object with email and password
     * @return jwt token for authenticated user
     * @throws Exception
     */
    @PostMapping(value = "/login")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    LoginResponseBean login(@RequestBody User user) throws Exception {
        return userDetailsService.login(user);
    }

    @PostMapping(value = "/activateUser")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    void activateUser(@RequestParam("userToken") UUID userToken, @RequestBody User user) throws Exception {
        log.info("Request activate user request for " + userToken);
        long startTime = System.currentTimeMillis();
        user.setUserUuid(userToken);
        userDetailsService.setPassword(user);
        log.info("Completed processing forgot password request in " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    @PutMapping(value="/forgotPassword")
    @ResponseStatus(value=HttpStatus.ACCEPTED)
    void forgotPassword(@RequestParam String email) throws Exception {
        log.info("Received forgot password request for " + email);
        long startTime = System.currentTimeMillis();
        userDetailsService.forgotPassword(email);
        log.info("Completed processing set password request in " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    @PostMapping(value="/createUser")
    @PreAuthorize("hasRole('" + IConstant.UserRole.Names.SUPER_ADMIN + "') or hasRole('" + IConstant.UserRole.Names.CLIENT_ADMIN + "') or hasRole('" + IConstant.UserRole.Names.RECRUITMENT_AGENCY + "')")
    String addUser(@RequestBody User user) throws Exception {
        return Util.stripExtraInfoFromResponseBean(
                userDetailsService.createUser(user),
                (new HashMap<String, List<String>>(){{
                    put("User", Arrays.asList("id", "firstName", "lastName", "email","mobile", "companyAddressId","companyBuId"));
                }}),
                null
        );
    }

    @PutMapping(value = "/blockUnblockUser")
    @PreAuthorize("hasRole('" + IConstant.UserRole.Names.SUPER_ADMIN + "') or hasRole('" + IConstant.UserRole.Names.CLIENT_ADMIN + "')")
    @ResponseStatus(value = HttpStatus.OK)
    void blockUser(@RequestBody User user, @RequestParam boolean blockUser) throws Exception {
        log.info("Received request to block user with id: "+ user.getId());
        long startTime = System.currentTimeMillis();
        userDetailsService.blockUser(user,blockUser);
        log.info("Complete block user request in " + (System.currentTimeMillis() - startTime) + "ms.");
    }
}
