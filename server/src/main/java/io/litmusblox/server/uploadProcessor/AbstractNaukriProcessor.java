/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.CandidateDetails;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;

import java.text.SimpleDateFormat;


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

        //clean mobile no in common place
        candidate.setMobile(naukriRow.getMobile());

        candidate.setTelephone(naukriRow.getTelephone());

        CandidateDetails candidateDetails = new CandidateDetails();
        candidateDetails.setCurrentAddress(naukriRow.getPostalAddress());
        if (!Util.isNull(naukriRow.getDOB()) && naukriRow.getDOB().trim().length() > 0)
            candidateDetails.setDateOfBirth(DATE_PARSER.parse(naukriRow.getDOB().replaceAll("'","").replaceAll("\"","")));
        //work experience - strip out Year(s) and Month(s) and generate a double value
        String[] workArray = naukriRow.getWorkExperience().split("\\s+");
        candidateDetails.setTotalExperience(Double.valueOf(workArray[0] + "."+workArray[2]));

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
