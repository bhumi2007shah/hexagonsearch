/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.service.IProcessOtpService;
import io.litmusblox.server.utils.RestClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * @author : Sumit
 * Date : 17/12/19
 * Time : 5:45 PM
 * Class Name : ProcessOtpService
 * Project Name : server
 */
@Log4j2
@Service
public class ProcessOtpService implements IProcessOtpService {

    @Autowired
    Environment environment;

    /**
     * Service method to handle send Otp request from search job page
     * @param mobile mobile number to send otp to
     * @param email email address to send otp to
     * @throws Exception
     */
    @Override
    public void sendOtp(String mobile, String email) throws Exception {
        log.info("Received request to Send OTP for mobile number {} and email {}", mobile, email);
        long startTime = System.currentTimeMillis();
        try {
            RestClient rest = RestClient.getInstance();
            StringBuilder stringBuilder = new StringBuilder(environment.getProperty(IConstant.OtpMsg91.SEND_OTP_URL.getValue()));
            stringBuilder.append("?authkey="+environment.getProperty(IConstant.OtpMsg91.AUTH_KEY.getValue()));
            stringBuilder.append("&mobile="+mobile);
            stringBuilder.append("&email="+email);
            stringBuilder.append("&template_id="+environment.getProperty(IConstant.OtpMsg91.TEMPLATE_ID.getValue()));
            stringBuilder.append("&sender="+environment.getProperty(IConstant.OtpMsg91.SENDER.getValue()));
            stringBuilder.append("&otp_length="+environment.getProperty(IConstant.OtpMsg91.OTP_LENGTH.getValue()));
            String response = rest.consumeRestApi(null, stringBuilder.toString(), HttpMethod.GET,null).getResponseBody();
            log.info("Response from resend otp api call: {}", response);
        }catch (Exception ex){
            log.error("Error while send otp : "+ex.getMessage());
        }
        log.info("Completed processing Send OTP request in {}",(System.currentTimeMillis() - startTime));
    }

    /**
     * Service method to validate Otp against a mobile number
     * @param mobile the mobile number for the otp
     * @param otp the otp value
     * @return boolean indicating whether the otp verification succeeded or failed
     * @throws Exception
     */
    @Override
    public boolean verifyOtp(String mobile, String otp){
        log.info("Received request to Verify OTP for mobile number {} with otp value {}", mobile, otp);
        long startTime = System.currentTimeMillis();
        boolean match = true;
        try {
            RestClient rest = RestClient.getInstance();
            StringBuilder stringBuilder = new StringBuilder(environment.getProperty(IConstant.OtpMsg91.VERIFY_OTP.getValue()));
            stringBuilder.append("?authkey="+environment.getProperty(IConstant.OtpMsg91.AUTH_KEY.getValue()));
            stringBuilder.append("&mobile="+mobile);
            stringBuilder.append("&otp="+otp);
            log.info("Msg91 request url: {}", stringBuilder.toString());
            String response = rest.consumeRestApi(null, stringBuilder.toString(), HttpMethod.POST,null).getResponseBody();
            if (null != response && response.indexOf("success") == -1)
            //if (!OTP_MATCH.equalsIgnoreCase(response))
                match = false;
            log.info("Response from msg91\n{}", response);
        }catch (Exception ex){
            log.error("Error while verify otp : "+ex.getMessage());
        }
        log.info("Completed processing Verify OTP request in {}",(System.currentTimeMillis() - startTime));
        return match;
    }
}
