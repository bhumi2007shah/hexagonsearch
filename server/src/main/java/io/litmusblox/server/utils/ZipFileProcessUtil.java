/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.service.CvUploadResponseBean;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author : Sumit
 * Date : 21/08/19
 * Time : 11:29 PM
 * Class Name : ZipFileProcessUtil
 * Project Name : server
 */
@Log4j2
public class ZipFileProcessUtil {

    public static Integer[] extractZipFile(String filePath, String tempRepoLocation, long loginUserId, long jobId, CvUploadResponseBean responseBean, Integer failureCount, Integer successCount) {

        String extension = Util.getFileExtension(filePath).toLowerCase();
        File newFile=null;
        if(extension.equals(IConstant.FILE_TYPE.zip.toString())){

            try {
                byte[] buffer = new byte[1024];

                //get the zip file content
                ZipInputStream zis = new ZipInputStream(new FileInputStream(filePath));

                //get the zipped file list entry
                ZipEntry ze = zis.getNextEntry();

                while(ze!=null){
                    String fileName = ze.getName();
                    String fileExtension=Util.getFileExtension(fileName);
                    if(!Arrays.asList(IConstant.cvUploadSupportedExtensions).contains(fileExtension)) {
                        failureCount++;
                        responseBean.getCvUploadMessage().put(fileName, IErrorMessages.UNSUPPORTED_FILE_TYPE +" "+fileExtension);
                    }else{
                        StringBuilder file=new StringBuilder();
                        fileName = Util.cleanFileName(fileName.split("/")[fileName.split("/").length-1]);
                        fileName=file.append(loginUserId).append("_").append(jobId).append("_").append(fileName).toString();
                        newFile = new File(tempRepoLocation + File.separator + fileName);
                        log.info("Zip file unzip : "+ newFile.getAbsoluteFile());
                        successCount++;
                        //create all non exists folders
                        //else you will hit FileNotFoundException for compressed folder
                        new File(newFile.getParent()).mkdirs();
                        FileOutputStream fos = new FileOutputStream(newFile);

                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                    }
                    ze = zis.getNextEntry();
                }

                zis.closeEntry();
                zis.close();
                new File(filePath).delete();
            } catch (IOException e) {
                failureCount++;
                responseBean.getCvUploadMessage().put(filePath, e.getMessage());
            }
        }else if(extension.equals(IConstant.FILE_TYPE.rar.toString())){

            try {
                File f = new File(filePath);
                Archive archive = new Archive(f);
                FileHeader fh = archive.nextFileHeader();
                while (fh != null) {
                    String fileName = fh.getFileNameString().trim();
                    String fileExtension=Util.getFileExtension(fileName);
                    if(!Arrays.asList(IConstant.cvUploadSupportedExtensions).contains(fileExtension)) {
                        failureCount++;
                        responseBean.getCvUploadMessage().put(fileName, IErrorMessages.UNSUPPORTED_FILE_TYPE +" "+fileExtension);
                    }else{
                        StringBuilder file = new StringBuilder();
                        fileName = file.append(loginUserId).append("_").append(jobId).append("_").append(fileName).toString();
                        newFile = new File(tempRepoLocation + File.separator + fileName);
                        log.info("Rar file unzip : " + newFile.getAbsoluteFile());
                        successCount++;
                        FileOutputStream os = new FileOutputStream(newFile);
                        archive.extractFile(fh, os);
                        os.close();
                    }
                    fh = archive.nextFileHeader();
                }
                new File(filePath).delete();
            } catch (IOException | RarException e) {
                failureCount++;
                responseBean.getCvUploadMessage().put(filePath, e.getMessage());
            }
        }
        return new Integer[]{failureCount,successCount};
    }

}
