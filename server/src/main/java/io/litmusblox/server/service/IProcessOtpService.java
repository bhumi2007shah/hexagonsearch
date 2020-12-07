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
     *
     * @param sendEmailOtp true if the send otp request was from employee referral flow
     * @param mobileNumber mobile number to send otp to
     * @param countryCode country code
     * @param email email address of the employee
     * @param recepientName name of the message receiver
     * @param companyShortName shortname of the company
     * @throws Exception
     */
    void sendOtp(boolean sendEmailOtp, String mobileNumber, String countryCode, String email, String recepientName, String companyShortName) throws Exception;

    /**
     * Service method to validate Otp against a mobile number
     * @param otpRequestKey the mobile number or  for the otp
     * @param otp the otp value
     * @return boolean indicating whether the otp verification succeeded or failed
     * @throws Exception
     */
    boolean verifyOtp(String otpRequestKey, int otp) throws Exception;
}