/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Sumit
 * Date : 01/08/19
 * Time : 10:57 AM
 * Class Name : StoreFileUtil
 * Project Name : server
 */
@Log4j2
public class StoreFileUtil {

    /**
     * storeFile method to save MultipartFile
     *
     * @param multipartFile which file we upload
     * @param id it is like userId or CompanyId
     * @param repoLocation location for save the file
     * @param uploadType which type of file we save
     * @return it return filepath string
     * @throws Exception
     */

    public static String storeFile(MultipartFile multipartFile, long id, String repoLocation, String uploadType, Candidate candidate, User user) throws Exception {
        File targetFile =  null;
        Boolean isZipFile=false;
        try {
            if(uploadType.equals(IConstant.FILE_TYPE.zip.toString()) || uploadType.equals(IConstant.FILE_TYPE.rar.toString())){
                isZipFile=true;
            }
            InputStream is = multipartFile.getInputStream();
            String filePath = getFileName(multipartFile.getOriginalFilename(), id, repoLocation, uploadType, (null!=candidate)?candidate.getId():(null!=user)?user.getId():null, isZipFile);
            //Util.storeFile(is, filePath,repoLocation);
            if(Util.isNull(filePath)){
                StringBuffer info = new StringBuffer(multipartFile.getName()).append(" FilePath is null ");
                log.info(info.toString());
                Map<String, String> breadCrumb = new HashMap<>();
                if(null!=user){
                    breadCrumb.put("User Id",user.getId().toString());
                    breadCrumb.put("User email",user.getEmail());
                    breadCrumb.put("User mobile", user.getMobile());
                }
                if(null!=candidate){
                    breadCrumb.put("Candidate Id",candidate.getId().toString());
                    breadCrumb.put("Candidate email",candidate.getEmail());
                    breadCrumb.put("Candidate Mobile",candidate.getMobile());
                }
                breadCrumb.put("filePath", filePath);
                breadCrumb.put("detail", info.toString());
                throw new WebException(IErrorMessages.INVALID_SETTINGS, HttpStatus.EXPECTATION_FAILED, breadCrumb);
            }
            targetFile = new File(repoLocation + File.separator + filePath);
            Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if(isZipFile)
                return targetFile.toString();

            return filePath;
        }
        catch (WebException e) {
            throw e;
        }
        catch (Exception e) {
            throw new WebException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR,e);
        }
    }

    private static String getFileName(String fileName, long id, String repoLocation, String uploadType, Long candidateId, Boolean isZipFile) throws Exception {

        try {
            StringBuffer filePath=new StringBuffer();
            String staticRepoPath = null;
            //Clean file name, remove all other than alphabet, digit, "_"
            fileName = Util.cleanFileName(fileName);
            if (Util.isNull(repoLocation)) {
                StringBuffer info = new StringBuffer(fileName).append(" repoLocation is null ");
                log.info(info.toString());
                Map<String, String> breadCrumb = new HashMap<>();
                breadCrumb.put("Candidate Id",candidateId.toString());
                breadCrumb.put("repoLocation", repoLocation);
                breadCrumb.put("detail", info.toString());
                throw new WebException(IErrorMessages.INVALID_SETTINGS, HttpStatus.EXPECTATION_FAILED, breadCrumb);
            }
            staticRepoPath = repoLocation;

            if(uploadType.equals(IConstant.ERROR_FILES)){
                filePath.append(uploadType).append(File.separator);
            }else if(!isZipFile && !uploadType.equals(IConstant.FILE_TYPE.other.toString())){
                filePath.append(uploadType).append(File.separator).append(id);
            }

            File file = new File(staticRepoPath + File.separator + filePath);
            if (!file.exists()) {
                file.mkdirs();
            }

           if(isZipFile){
                filePath.append(File.separator).append(fileName);
            }else if(uploadType.equals(IConstant.FILE_TYPE.other.toString())){
                filePath.append(candidateId).append("_").append(id).append("_").append(fileName);
            }else if(uploadType.equals(IConstant.ERROR_FILES)){
               filePath.append(fileName);
            }else if(null!=candidateId){
                filePath.append(File.separator).append(candidateId).append(".").append(Util.getFileExtension(fileName));
            }else{
                filePath.append(File.separator).append(fileName.substring(0,fileName.indexOf('.'))).append("_").append(Util.formatDate(new Date(), IConstant.DATE_FORMAT_yyyymmdd_hhmm)).append(".").append(Util.getFileExtension(fileName));
            }

            log.info("Saved file: "+filePath);
            return filePath.toString();
        }
        catch (Exception e) {
            log.error(e.getMessage());
            throw new WebException(IErrorMessages.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

}
