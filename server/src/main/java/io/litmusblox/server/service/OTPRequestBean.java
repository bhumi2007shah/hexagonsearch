/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : Shital Raval
 * Date : 19/2/20
 * Time : 11:49 AM
 * Class Name : OTPRequestBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
public class OTPRequestBean {
    private int otp;
    private int otpExpiry;
    private String receivermobile;
    private String receiveremail;

    //Hard-coded to get around not null checks for the following in communication module
    private String receiverfirstName = "-";
    private String receivercountrycode = "+91";
    private String sendercountrycode = "+91";

    public OTPRequestBean(int otp, int otpExpiry, String receivermobile, String receiveremail) {
        this.otp = otp;
        this.otpExpiry = otpExpiry;
        this.receivermobile = receivermobile;
        this.receiveremail = receiveremail;
    }
}
