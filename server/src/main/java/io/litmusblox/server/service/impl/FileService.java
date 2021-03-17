/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.utils.Base64Util;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author : sameer
 * Date : 04/02/21
 * Time : 10:07 AM
 * Class Name : FileService
 * Project Name : server
 */

@Service
@Log4j2
public class FileService  {
    public static MultipartFile convertBase64ToMultipart(String fileContent,String fileName)  {
        MultipartFile decodedFile = null;

        try{
            if(!fileContent.isEmpty()){
                byte [] decodedFileBytes = Base64Util.decode(fileContent);

                try(GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(decodedFileBytes))){
                    byte [] decodedBytes = gzipInputStream.readAllBytes();
                   decodedFile = Util.convertByteStreamToMultipartFile(decodedBytes,fileName);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return decodedFile;
    }
}
