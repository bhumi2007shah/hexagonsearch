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
    private String receiverfirstname;
    private String receivercountrycode;
    private String sendercountrycode;
    private String sendercompany;

    public OTPRequestBean(int otp, int otpExpiry, String receivermobile, String receivercountrycode, String receiveremail, String receiverfirstname, String sendercompany) {
        this.otp = otp;
        this.otpExpiry = otpExpiry;
        this.receivermobile = receivermobile;
        this.receiveremail = receiveremail;
        this.receivercountrycode = receivercountrycode;
        this.sendercountrycode = receivercountrycode;
        this.receiverfirstname = receiverfirstname;
        this.sendercompany = sendercompany;
    }
}
