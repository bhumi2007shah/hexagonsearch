/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.model.Company;
import io.litmusblox.server.repository.CompanyRepository;
import io.litmusblox.server.service.IProcessOtpService;
import io.litmusblox.server.service.OTPRequestBean;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private Queue queue;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Resource
    CompanyRepository companyRepository;

    private LoadingCache<String, Integer> otpCache;

    //initialize cache
    public ProcessOtpService() {
        log.info("Initializing cache for OTP");
        otpCache = CacheBuilder.newBuilder()
                .expireAfterWrite(IConstant.OTP_EXPIRY_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    /**
     * Service method to handle send Otp request from search job page
     *
     * @param isEmployeeReferral true if the send otp request was from employee referral flow
     * @param mobileNumber mobile number to send otp to
     * @param countryCode country code
     * @param email email address of the employee
     * @param recepientName name of the message receiver
     * @param companyShortName shortname of the company
     * @throws Exception
     */
    @Override
    public void sendOtp(boolean isEmployeeReferral, String mobileNumber, String countryCode, String email, String recepientName, String companyShortName) throws Exception {
        log.info("Received request to Send OTP for {} mobile: {} email: {} ", recepientName, mobileNumber, email);
        long startTime = System.currentTimeMillis();

        if(isEmployeeReferral && (null == email || email.trim().length() == 0))
            throw new ValidationException("Email address is required for Employee Referral OTP", HttpStatus.UNPROCESSABLE_ENTITY);

        if(!isEmployeeReferral && (null == mobileNumber || mobileNumber.trim().length() == 0))
            throw new ValidationException("Mobile number is required to send OTP", HttpStatus.UNPROCESSABLE_ENTITY);

        //Retrieve the company name based on company short name
        Company companyObjToUse = companyRepository.findByShortNameIgnoreCase(companyShortName);
        if (null == companyObjToUse)
            throw new ValidationException("No company found for short name:"+companyShortName, HttpStatus.UNPROCESSABLE_ENTITY);

        String otpRequestKey = isEmployeeReferral?email:mobileNumber;

        Random random = new Random();
        int otp = 0;
        while (otp == 0 || otp >= 10000)
            otp = 1000 + random.nextInt(10000);
        otpCache.put(otpRequestKey, otp);
        log.info("Generated otp: {} for {}", otp, otpRequestKey);

        //TODO: Push the otp on to queue
        ObjectMapper objectMapper = new ObjectMapper();
        //Messages on queue that are more than timeout seconds old, should not be processed
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setTimeToLive(IConstant.OTP_EXPIRY_MINUTES * 60 * 1000);

        OTPRequestBean otpRequestBean;
        //if the otp is for employee referral, do not send mobile to queue
        //if the otp is for candidate career page, do not send email to queue
        if (isEmployeeReferral)
            otpRequestBean = new OTPRequestBean(otp, IConstant.OTP_EXPIRY_MINUTES, null, countryCode, email, recepientName, companyObjToUse.getCompanyName());
        else
            otpRequestBean = new OTPRequestBean(otp, IConstant.OTP_EXPIRY_MINUTES, mobileNumber, countryCode, null, recepientName, companyObjToUse.getCompanyName());

        jmsTemplate.convertAndSend(queue, objectMapper.writeValueAsString(otpRequestBean));
        log.info("Put message on queue {}", queue.getQueueName());
        log.info("Completed processing Send OTP request in {} ms",(System.currentTimeMillis() - startTime));
    }

    /**
     * Service method to validate Otp against a mobile number
     *
     * @param otpRequestKey the mobile number or  for the otp
     * @param otp           the otp value
     * @return boolean indicating whether the otp verification succeeded or failed
     * @throws Exception
     */
    @Override
    public boolean verifyOtp(String otpRequestKey, int otp) throws Exception {
        return (otpCache.get(otpRequestKey) == otp);
    }

    //This method is used to clear the OTP cached already
    public void clearOTP(String key){
        otpCache.invalidate(key);
    }
}
