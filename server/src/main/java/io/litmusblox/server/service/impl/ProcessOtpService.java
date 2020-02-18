/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.service.IProcessOtpService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

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

    private LoadingCache<String, Integer> otpCache;

    //initialize cache
    public ProcessOtpService() {
        log.info("Initializing cache for OTP");
        otpCache = CacheBuilder.newBuilder()
                .expireAfterWrite(IConstant.OTP_EXPIRY_SECONDS, TimeUnit.SECONDS)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    /**
     * Service method to handle send Otp request from search job page
     *
     * @param otpRequestKey can be either mobile number or the email address to send otp to
     * @throws Exception
     */
    @Override
    public void sendOtp(String otpRequestKey) throws Exception {
        log.info("Received request to Send OTP for {}", otpRequestKey);
        long startTime = System.currentTimeMillis();

        Random random = new Random();
        int otp = 0;
        while (otp == 0 || otp >= 10000)
            otp = 1000 + random.nextInt(10000);
        otpCache.put(otpRequestKey, otp);
        log.info("Generated otp: {} for {}", otp, otpRequestKey);

        //TODO: Push the otp on to queue

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
