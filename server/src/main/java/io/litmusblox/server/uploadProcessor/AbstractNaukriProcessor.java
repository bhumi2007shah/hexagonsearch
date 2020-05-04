/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.CandidateDetails;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.http.HttpStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author : Sumit
 * Date : 19/7/19
 * Time : 3:30 PM
 * Class Name : AbstractNaukriProcessor
 * Project Name : server
 */
@Log4j2
public abstract class AbstractNaukriProcessor {

    private static SimpleDateFormat DATE_PARSER = new SimpleDateFormat("dd MMM yyyy");
    protected void convertNaukriRowToCandidate(Candidate candidate, NaukriFileRow naukriRow) throws Exception {

        Util.handleCandidateName(candidate, naukriRow.getCandidateName());

        candidate.setEmail(naukriRow.getEmail());
        log.info("The Email has been set {}", naukriRow.getEmail());
        //clean mobile no in common place
        candidate.setMobile(naukriRow.getMobile());
        log.info("The Mobile has been set {}", naukriRow.getMobile());

        candidate.setTelephone(naukriRow.getTelephone());
        log.info("The Telephone has been set {}", naukriRow.getTelephone());

        CandidateDetails candidateDetails = new CandidateDetails();
        candidateDetails.setCurrentAddress(naukriRow.getPostalAddress());
        log.info("The Postal Address has been set {}", naukriRow.getPostalAddress());

        //Logic for DoB formatting starts here
        if (!Util.isNull(naukriRow.getDOB()) && naukriRow.getDOB().trim().length() > 0) {
            String dobString = null;
            Date candidateDOB = new Date();
            for(String dateFormatRegex:IConstant.DATE_FORMAT_REGEX_MAP.keySet()){
                Pattern pattern = Pattern.compile(dateFormatRegex);
                Matcher matcher = pattern.matcher(naukriRow.getDOB());
                if(matcher.find()){
                    dobString=matcher.group().trim();
                    try
                    {
                        log.info("The DOB fetched is {}", dobString);
                        if(!dobString.equals((null))) {
                            candidateDOB = new SimpleDateFormat(IConstant.DATE_FORMAT_REGEX_MAP.get(dateFormatRegex)).parse(dobString.replaceAll("'", "").replaceAll("\"", ""));
                            if (!candidateDOB.equals(null)) {
                                candidateDetails.setDateOfBirth(candidateDOB);
                                log.info("The DOB has been set as {}", candidateDOB);
                                break;
                            }
                        }
                    }
                    catch (ParseException e) {
                        log.info("DOB format is invalid for the candidate {} with Mobile number {}", naukriRow.getCandidateName(),naukriRow.getMobile());
                        throw new ValidationException(IErrorMessages.INVALID_DATE_OF_BIRTH_FORMAT + " - " + naukriRow.getDOB(), HttpStatus.BAD_REQUEST);
                    }
                }
            }
        }
        //work experience - strip out Year(s) and Month(s) and generate a double value
//        log.info("The Work Exp fetched is {}",naukriRow.getWorkExperience());
        if(!Util.isNull(naukriRow.getWorkExperience())) {
//            To remove any letters like Y/M/Years/Months from the Work experience String
            String workExperience = naukriRow.getWorkExperience().replaceAll("[a-zA-Z]{1,}","").trim();
//            log.info("The replaced Work Exp is: {}",workExp);
            String[] workArray = workExperience.split(" ");
            log.info("The Years is {} and Months is {}", workArray[0],workArray[1]);
            candidateDetails.setTotalExperience(Double.valueOf(workArray[0] + "." + workArray[1]));
        }
        candidateDetails.setResumeHeadline(naukriRow.getResumeTitle());
        candidateDetails.setLocation(naukriRow.getCurrentLocation());
        candidateDetails.setPreferredLocations(naukriRow.getPreferredLocation());

        //TODO:Create and set CandidateCompanyDetails and CandidateEducationDetails

        /*CandidateCompanyDetails candidateCompanyDetails = new CandidateCompanyDetails();
        candidateCompanyDetails.setCompanyName(naukriRow.getCurrentEmployer());
        candidateCompanyDetails.setDesignation(naukriRow.getCurrentDesignation());
        candidateCompanyDetails.setSalary(naukriRow.getAnnualSalary());

        List<CandidateEducationDetails> candidateEducationDetailList = new ArrayList<>(0);
        if (!Util.isNull(naukriRow.getUGCourse()))
            candidateEducationDetailList.add(new CandidateEducationDetails(naukriRow.getUGCourse()));
        if (!Util.isNull(naukriRow.getPGCourse()))
            candidateEducationDetailList.add(new CandidateEducationDetails(naukriRow.getPGCourse()));
        if (!Util.isNull(naukriRow.getPPGCourse()))
            candidateEducationDetailList.add(new CandidateEducationDetails(naukriRow.getPPGCourse()));

        candidateDetails.setLastActive(DATE_PARSER.parse(naukriRow.getLastActive().replaceAll("'","").replaceAll("\"","")));

        candidate.setCandidateDetails(candidateDetails);
        candidate.setCandidateCompanyDetails(Arrays.asList(candidateCompanyDetails));
        if(candidateEducationDetailList.size() > 0)
            candidate.setCandidateEducationDetails(candidateEducationDetailList);*/
    }

    protected boolean checkForCells(Row row) {
        IConstant.NAUKRI_FILE_COLUMNS[] fileColumns = IConstant.NAUKRI_FILE_COLUMNS.values();
        for (int i = 0; i < fileColumns.length; i++) {
            if(null == row.getCell(i).getStringCellValue() || (!row.getCell(i).getStringCellValue().trim().equalsIgnoreCase(fileColumns[i].getValue())))
                return false;
        }
        return true;
    }
}
