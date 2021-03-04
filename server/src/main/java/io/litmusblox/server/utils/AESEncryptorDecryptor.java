/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import io.litmusblox.server.constant.IConstant;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author : sameer
 * Date : 02/03/21
 * Time : 4:26 PM
 * Class Name : AESEncryptorDecryptor
 * Project Name : server
 */
@Service
public class AESEncryptorDecryptor {

    public static byte[] generateKey(int n) throws NoSuchAlgorithmException{
        KeyGenerator keyGenerator = KeyGenerator.getInstance(IConstant.algorithmType);
        keyGenerator.init(n);
        return keyGenerator.generateKey().getEncoded();
    }

    public static String encrypt(String input, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(IConstant.algorithmType);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder()
                .encodeToString(cipherText);
    }

    public static String decrypt(String cipherText, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(IConstant.algorithmType);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainText = cipher.doFinal(Base64.getDecoder()
                .decode(cipherText));
        return new String(plainText);
    }
}
