/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.processOtp;

import io.litmusblox.server.constant.IConstant;
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
public class ProcessOtpService {

    @Autowired
    Environment environment;

    public String sendOtp(String mobile, String email, int countryCode) throws Exception {
        String otpResponseBean = null;
        try {
            RestClient rest = RestClient.getInstance();
            StringBuilder stringBuilder = new StringBuilder(environment.getProperty(IConstant.SEND_OTP_URL));
            stringBuilder.append("?authkey="+environment.getProperty(IConstant.AUTH_KEY));
            stringBuilder.append("&mobile="+mobile);
            stringBuilder.append("&email="+email);
            stringBuilder.append("&template_id="+environment.getProperty(IConstant.TEMPLATE_ID));
            stringBuilder.append("&sender="+environment.getProperty(IConstant.SENDER));
            stringBuilder.append("&otp_expiry=");
            stringBuilder.append("&otp_length="+environment.getProperty(IConstant.OTP_LENGTH));
            stringBuilder.append("&country="+countryCode);
            stringBuilder.append("&otp=");
            otpResponseBean = rest.consumeRestApi(null, stringBuilder.toString(), HttpMethod.POST,null);
        }catch (Exception ex){
            log.error("Error while send otp : "+ex.getMessage());
        }
        return otpResponseBean;
    }

    public String verifyOtp(String mobile, String otp){
        String otpResponseBean = null;
        try {
            RestClient rest = RestClient.getInstance();
            StringBuilder stringBuilder = new StringBuilder(environment.getProperty(IConstant.VERIFY_OTP));
            stringBuilder.append("?authkey="+environment.getProperty(IConstant.AUTH_KEY));
            stringBuilder.append("&mobile="+mobile);
            stringBuilder.append("&otp="+otp);
            otpResponseBean = rest.consumeRestApi(null, stringBuilder.toString(), HttpMethod.POST,null);
        }catch (Exception ex){
            log.error("Error while verify otp : "+ex.getMessage());
        }
        return otpResponseBean;
    }

    public String retryOtp(String mobile){
        String otpResponseBean = null;
        try {
            RestClient rest = RestClient.getInstance();
            StringBuilder stringBuilder = new StringBuilder(environment.getProperty(IConstant.RETRY_OTP));
            stringBuilder.append("?authkey="+environment.getProperty(IConstant.AUTH_KEY));
            stringBuilder.append("&mobile="+mobile);
            stringBuilder.append("&retrytype="+environment.getProperty(IConstant.RETRY_TYPE));
            otpResponseBean = rest.consumeRestApi(null, stringBuilder.toString(), HttpMethod.POST,null);
        }catch (Exception ex){
            log.error("Error while retry otp : "+ex.getMessage());
        }
        return otpResponseBean;
    }


}
