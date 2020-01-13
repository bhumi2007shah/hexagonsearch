/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.processOtp.ProcessOtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author : Sumit
 * Date : 18/12/19
 * Time : 12:21 AM
 * Class Name : OtpProcessingController
 * Project Name : server
 */
@RestController
@RequestMapping("/api/otp")
public class OtpProcessingController {

    @Autowired
    ProcessOtpService processOtpService;

    @GetMapping(value = "/sendOtp")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    String sendOtp(@RequestParam String mobile, @RequestParam String email, @RequestParam Integer countryCode) throws Exception {
        return processOtpService.sendOtp(mobile, email, countryCode);
    }

    @GetMapping(value = "/verifyOtp")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    String verifyOtp(@RequestParam String mobile, @RequestParam String otp) throws Exception {
        return processOtpService.verifyOtp(mobile, otp);
    }

    @GetMapping(value = "/retryOtp")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    String retryOtp(@RequestParam String mobile) throws Exception {
        return processOtpService.retryOtp(mobile);
    }

}
