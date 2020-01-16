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
            String response = rest.consumeRestApi(null, stringBuilder.toString(), HttpMethod.GET,null);
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
    //private static String OTP_MATCH = "{\"message\":\"OTP verified success\",\"type\":\"success\"}\n";
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
            String response = rest.consumeRestApi(null, stringBuilder.toString(), HttpMethod.POST,null);
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

    /**
     * Service method to handle request for resend otp for a mobile number
     * @param mobile the mobile number for which the otp needs to be resent
     * @throws Exception
     */
    //Since we are not getting sms and email for a resend request to Msg91, commenting out this api
    /*@Override
    public void resendOtp(String mobile){
        log.info("Received request to Resend OTP for mobile number {}", mobile);
        long startTime = System.currentTimeMillis();
        try {
            RestClient rest = RestClient.getInstance();
            StringBuilder stringBuilder = new StringBuilder(environment.getProperty(IConstant.OtpMsg91.RETRY_OTP.getValue()));
            stringBuilder.append("?authkey="+environment.getProperty(IConstant.OtpMsg91.AUTH_KEY.getValue()));
            stringBuilder.append("&mobile="+mobile);
            stringBuilder.append("&retrytype="+environment.getProperty(IConstant.OtpMsg91.RETRY_TYPE.getValue()));
            String response = rest.consumeRestApi(null, stringBuilder.toString(), HttpMethod.POST,null);
            log.info("Response from resend otp api call: {}", response);
        }catch (Exception ex){
            log.error("Error while retry otp : "+ex.getMessage());
        }
        log.info("Completed processing Resend OTP request in {}",(System.currentTimeMillis() - startTime));
    }*/
}
