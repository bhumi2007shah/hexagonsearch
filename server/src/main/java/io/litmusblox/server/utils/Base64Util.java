/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import java.util.Base64;

/**
 * @author : sameer
 * Date : 04/02/21
 * Time : 10:22 AM
 * Class Name : Base64Util
 * Project Name : server
 */
public class Base64Util {

    public static byte [] decode(String encodedBase64File){
        byte [] fileBytes = null;
        try {
            fileBytes= Base64.getDecoder().decode(encodedBase64File);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return fileBytes;
    }
}
