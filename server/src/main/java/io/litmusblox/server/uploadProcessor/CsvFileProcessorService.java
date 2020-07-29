/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.User;
import io.litmusblox.server.service.UploadResponseBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;


/**
 * @author : Sumit
 * Date : 19/7/19
 * Time : 3:31 PM
 * Class Name : CsvFileProcessorService
 * Project Name : server
 */
@Log4j2
public class CsvFileProcessorService extends AbstractNaukriProcessor implements IUploadFileProcessorService {

    @Transactional
    @Override
    public List<Candidate> process(String fileName, UploadResponseBean responseBean, boolean ignoreMobile, String repoLocation, User loggedInUser, String fileType) {
        List<Candidate> candidateList = new ArrayList<>();
        try {

            Reader fileReader = new FileReader(repoLocation + File.separator + fileName);

            // create csvReader object with parameter fileReader
            CSVReader csvReader = new CSVReaderBuilder(fileReader).build();

            // Read all data at once
            List<String[]> allData = csvReader.readAll();

            Map<String, Integer> headers = new HashMap<>();
            Map<String, String> breadCrumb= new HashMap<>();
            breadCrumb.put("File Name", fileName);
            breadCrumb.put("File Type",IConstant.PROCESS_FILE_TYPE.CsvFile.toString());
            breadCrumb.put("File upload type", fileType);
            boolean downloadedFlag = false;
            int rowCount = 0;

            for (String[] record : allData) {

                //Check for naukri file is there any data other than header
                if(rowCount<2){
                    if(rowCount==0 && record[0].toLowerCase().contains("download")){
                        log.info("'Downloaded' exists. Skipping first two rows.");
                        downloadedFlag=true;
                    }
                    if(downloadedFlag) {
                        rowCount++;
                        continue; //skip first 2 rows
                    }
                }

                if(IConstant.UPLOAD_FORMATS_SUPPORTED.LitmusBlox.name().equalsIgnoreCase(fileType)){
                    if(headers.size()==0){
                        for(IConstant.LITMUSBLOX_FILE_COLUMNS litmusCol : IConstant.LITMUSBLOX_FILE_COLUMNS.values()){
                            for(String cellValue : record){
                                if(cellValue.equalsIgnoreCase(litmusCol.getValue())){
                                    headers.put(litmusCol.name(), ArrayUtils.indexOf(record, cellValue));
                                    continue;
                                }
                            }
                            breadCrumb.put(litmusCol.getValue(), (null != headers.get(litmusCol.name()))?headers.get(litmusCol.name()).toString():"");
                        }
                        if(headers.size()<4)
                            throw new WebException(IConstant.UPLOAD_FORMATS_SUPPORTED.LitmusBlox.toString() + IErrorMessages.MISSING_COLUMN_NAMES_FIRST_ROW, HttpStatus.UNPROCESSABLE_ENTITY, breadCrumb);

                    }else{
                        try {
                            Candidate candidate = new Candidate(Util.toSentenceCase(record[headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.FirstName.name())].trim()),
                                    Util.toSentenceCase(record[headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.LastName.name())].trim()),
                                    record[headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.Email.name())].trim(),
                                    record[headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.Mobile.name())].trim(),                                    loggedInUser.getCountryId().getCountryCode(),
                                    new Date(),
                                    loggedInUser);
                            candidate.setCandidateSource(IConstant.CandidateSource.File.getValue());
                            candidateList.add(candidate);
                        } catch (Exception pe) {
                            log.error("Error while processing row from CSV file: " + pe.getMessage());
                            Candidate candidate = new Candidate();
                            candidate.setFirstName(Util.toSentenceCase(record[headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.FirstName.name())].trim()));
                            candidate.setLastName(Util.toSentenceCase(record[headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.LastName.name())].trim()));
                            candidate.setEmail(record[headers.get(IConstant.LITMUSBLOX_FILE_COLUMNS.Email.name())].trim());
                            if(ignoreMobile) {
                                candidateList.add(candidate);
                            }
                            else {
                                candidate.setUploadErrorMessage(IErrorMessages.MOBILE_NULL_OR_BLANK);
                                responseBean.getFailedCandidates().add(candidate);
                                responseBean.setFailureCount(responseBean.getFailureCount() + 1);
                            }
                        }
                    }
                }else if(IConstant.UPLOAD_FORMATS_SUPPORTED.Naukri.name().equalsIgnoreCase(fileType)){
                    if(headers.size()==0){
                        for(Map.Entry<String, ArrayList<String>> NaukriMap : IConstant.NAUKRI_FILE_COLUMNS_MAP.entrySet()){
                            for(String cellValue : record){
                                if(NaukriMap.getValue().contains(cellValue)){
                                    headers.put(NaukriMap.getKey(), ArrayUtils.indexOf(record, cellValue));
                                    //continue;
                                }
                            }
                            breadCrumb.put(NaukriMap.getKey(), (null != headers.get(NaukriMap.getKey()))?headers.get(NaukriMap.getKey()).toString():"");
                        }
                        if(headers.size()<19)
                            throw new WebException(IConstant.UPLOAD_FORMATS_SUPPORTED.Naukri.toString() + IErrorMessages.MISSING_COLUMN_NAMES_FIRST_ROW, HttpStatus.UNPROCESSABLE_ENTITY, breadCrumb);
                    }else{
                        try {
                            NaukriFileRow naukriRow = new NaukriFileRow();
                            naukriRow.setSerialNumber(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.SerialNumber.name())].trim());
                            naukriRow.setCandidateName(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.CandidateName.name())].trim());
                            naukriRow.setResumeId(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.ResumeId.name())].trim());
                            naukriRow.setPostalAddress(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.PostalAddress.name())].trim());
                            naukriRow.setTelephone(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.Telephone.name())].trim());
                            naukriRow.setMobile(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.Mobile.name())].trim());
                            naukriRow.setDOB(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.DOB.name())].trim());
                            naukriRow.setEmail(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.Email.name())].trim());
                            naukriRow.setWorkExperience(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.WorkExperience.name())].trim());
                            naukriRow.setResumeTitle(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.ResumeTitle.name())].trim());
                            naukriRow.setCurrentLocation(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.CurrentLocation.name())].trim());
                            naukriRow.setPreferredLocation(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.PreferredLocation.name())].trim());
                            naukriRow.setCurrentEmployer(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.CurrentEmployer.name())].trim());
                            naukriRow.setCurrentDesignation(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.CurrentDesignation.name())].trim());
                            naukriRow.setAnnualSalary(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.AnnualSalary.name())].trim());
                            naukriRow.setUGCourse(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.UGCourse.name())].trim());
                            naukriRow.setPGCourse(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.PGCourse.name())].trim());
                            naukriRow.setPPGCourse(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.PPGCourse.name())].trim());
                            naukriRow.setLastActive(record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.LastActive.name())].trim());
                            Candidate candidate = new Candidate();
                            candidate.setCandidateSource(IConstant.CandidateSource.File.getValue());
                            convertNaukriRowToCandidate(candidate, naukriRow);
                            candidateList.add(candidate);
                        }catch (Exception ex){
                            log.error("Error while processing candidate email : {} mobile : {}, error message : ",record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.Email.name())], record[headers.get(IConstant.NAUKRI_FILE_COLUMNS.Mobile.name())], ex.getMessage());
                        }
                    }
                }
            }
        } catch(WebException we) {
            log.error("Error while parsing file " + fileName + " :: " + we.getMessage());
            throw we;
        } catch(IOException ioe) {
            log.error("Error while parsing file " + fileName + " :: " + ioe.getMessage());
            responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Error while processing file " + fileName + " :: " + ex.getMessage());
            responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        }
        return candidateList;
    }
}
