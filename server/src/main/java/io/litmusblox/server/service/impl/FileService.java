/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.service.IFileService;
import io.litmusblox.server.utils.Base64Util;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class FileService implements IFileService {
    @Override
    public ResponseEntity<Resource> convertToFile(MultipartFile multipartFile) throws Exception {
        ByteArrayResource decodedFile = null;
        HttpHeaders headers = new HttpHeaders();
        try{
            if(!multipartFile.isEmpty()){
                byte [] fileBytes = multipartFile.getBytes();
                String completeData = new String(fileBytes);
                String [] rows = completeData.split("[\n|\r]");

                for ( long i=0; i<rows.length; ++i){
                    if(i>0){
                        String [] columns = rows[(int) i].split(",");
                        String fileName = columns[2];
                        String fileContent = columns[3];
                        String attachmentType = columns[4];
                        String fileExtension = Util.getFileExtension(fileName);

                        byte [] decodedFileBytes = Base64Util.decode(fileContent);

                        try(GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(decodedFileBytes))){
                            byte [] decodedBytes = gzipInputStream.readAllBytes();
                            decodedFile = new ByteArrayResource(decodedBytes){
                                @Override
                                public String getFilename() {
                                    return (fileName);
                                }

                                @Override
                                public long contentLength() {
                                    return decodedBytes.length;
                                }
                            };

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+fileName+"\"");
                    }
                }
            }
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(decodedFile.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(decodedFile);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
