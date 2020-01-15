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
     * @param mobile mobile number to send otp to
     * @param email email address to send otp to
     * @param countryCode country code of the mobile
     * @throws Exception
     */
    void sendOtp(String mobile, String email, int countryCode) throws Exception;

    /**
     * Service method to validate Otp against a mobile number
     * @param mobile the mobile number for the otp
     * @param otp the otp value
     * @return boolean indicating whether the otp verification succeeded or failed
     * @throws Exception
     */
    boolean verifyOtp(String mobile, String otp);

    /**
     * Service method to handle request for resend otp for a mobile number
     * @param mobile the mobile number for which the otp needs to be resent
     * @throws Exception
     */
    void resendOtp(String mobile);
}
