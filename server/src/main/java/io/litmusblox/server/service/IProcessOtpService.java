/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

/**
 * @author : Shital Raval
 * Date : 13/1/20
 * Time : 2:25 PM
 * Class Name : IProcessOtpService
 * Project Name : server
 */
public interface IProcessOtpService {
    /**
     * Service method to handle send Otp request from search job page
     * @param otpRequestKey can be either mobile number or the email address to send otp to
     * @throws Exception
     */
    void sendOtp(String otpRequestKey) throws Exception;

    /**
     * Service method to validate Otp against a mobile number
     * @param otpRequestKey the mobile number or  for the otp
     * @param otp the otp value
     * @return boolean indicating whether the otp verification succeeded or failed
     * @throws Exception
     */
    boolean verifyOtp(String otpRequestKey, int otp) throws Exception;
}