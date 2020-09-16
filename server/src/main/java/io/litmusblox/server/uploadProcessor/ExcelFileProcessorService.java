/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.CandidateEmailHistory;
import io.litmusblox.server.model.CandidateMobileHistory;
import io.litmusblox.server.model.User;
import io.litmusblox.server.repository.CandidateEmailHistoryRepository;
import io.litmusblox.server.repository.CandidateMobileHistoryRepository;
import io.litmusblox.server.service.UploadResponseBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.http.HttpStatus;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author : Sumit
 * Date : 19/7/19
 * Time : 3:38 PM
 * Class Name : ExcelFileProcessorService
 * Project Name : server
 */


@Log4j2
public class ExcelFileProcessorService implements IUploadFileProcessorService {

    @Resource
    CandidateEmailHistoryRepository candidateEmailHistoryRepository;

    @Resource
    CandidateMobileHistoryRepository candidateMobileHistoryRepository;

    @Override
    public List<Candidate> process(String fileName, UploadResponseBean responseBean, boolean ignoreMobile,String repoLocation, User loggedInUser, String fileType) {
        log.info("Processing " + fileName);
        List<Candidate> candidateList = new ArrayList<>();
        try {

            Workbook workbook =  WorkbookFactory.create(new File(repoLocation + File.separator + fileName));

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();

            boolean firstRow = true;
            for (Row row : sheet) {
                if (firstRow) {
                    try {
                        //check that the row contains column names in correct order
                        if ((row.getPhysicalNumberOfCells() == 0) ||
                                (null == row.getCell(0).getStringCellValue().trim()) || (!row.getCell(0).getStringCellValue().equalsIgnoreCase(IConstant.LITMUSBLOX_FILE_COLUMNS.FirstName.getValue())) ||
                                (null == row.getCell(1).getStringCellValue().trim()) || (!row.getCell(1).getStringCellValue().equalsIgnoreCase(IConstant.LITMUSBLOX_FILE_COLUMNS.LastName.getValue())) ||
                                (null == row.getCell(2).getStringCellValue().trim()) || (!row.getCell(2).getStringCellValue().equalsIgnoreCase(IConstant.LITMUSBLOX_FILE_COLUMNS.Email.getValue())) ||
                                (null == row.getCell(3).getStringCellValue().trim()) || (!row.getCell(3).getStringCellValue().equalsIgnoreCase(IConstant.LITMUSBLOX_FILE_COLUMNS.Mobile.getValue()))){

                            Map<String, String> breadCrumb = new HashMap<>();
                            breadCrumb.put("File Name", fileName);
                            breadCrumb.put(IConstant.LITMUSBLOX_FILE_COLUMNS.FirstName.getValue(), row.getCell(0).getStringCellValue());
                            breadCrumb.put(IConstant.LITMUSBLOX_FILE_COLUMNS.LastName.getValue(), row.getCell(1).getStringCellValue());
                            breadCrumb.put(IConstant.LITMUSBLOX_FILE_COLUMNS.Email.getValue(), row.getCell(2).getStringCellValue());
                            breadCrumb.put(IConstant.LITMUSBLOX_FILE_COLUMNS.Mobile.getValue(), row.getCell(3).getStringCellValue());
                            breadCrumb.put("File Type", IConstant.PROCESS_FILE_TYPE.ExcelFile.toString());
                            throw new WebException(IConstant.UPLOAD_FORMATS_SUPPORTED.LitmusBlox.toString() + IErrorMessages.MISSING_COLUMN_NAMES_FIRST_ROW, HttpStatus.UNPROCESSABLE_ENTITY, breadCrumb);
                        }
                    } catch (Exception e) {
                        throw new WebException(IConstant.UPLOAD_FORMATS_SUPPORTED.LitmusBlox.toString() + IErrorMessages.MISSING_COLUMN_NAMES_FIRST_ROW, HttpStatus.UNPROCESSABLE_ENTITY);
                    }
                    firstRow = false;
                    continue;
                }
                if (row.getPhysicalNumberOfCells() > 0) {
                    Candidate candidate = new Candidate();
                    candidate.setCandidateSource(IConstant.CandidateSource.File.getValue());
                    boolean discardRow = true;
                    for (Cell cell : row) {
                        String cellValue = dataFormatter.formatCellValue(cell);
                        if (Util.isNotNull(cellValue) && discardRow)
                            discardRow = false;
                        switch (cell.getAddress().getColumn()) {
                            case 0:
                                candidate.setFirstName(Util.toSentenceCase(cellValue.trim()));
                                break;
                            case 1:
                                candidate.setLastName(Util.toSentenceCase(cellValue.trim()));
                                break;
                            case 2:
                                candidate.setEmail(cellValue.trim());
                                break;
                            case 3:
                                candidate.setMobile(row.getCell(3).toString().trim());
                        }
                    }
                    if (!discardRow)
                        candidateList.add(candidate);
                }
            }

        } catch(WebException we) {
            log.error("Error while parsing file " + fileName + " :: " + we.getMessage());
            throw we;
        }catch(IOException ioe) {
            log.error("Error while parsing file " + fileName + " :: " + ioe.getMessage());
            throw new WebException(IConstant.UPLOAD_FORMATS_SUPPORTED.LitmusBlox.toString() + IErrorMessages.MISSING_COLUMN_NAMES_FIRST_ROW, HttpStatus.UNPROCESSABLE_ENTITY);
            //responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        } /*catch (InvalidFormatException e) {
            logger.error("Error while parsing file " + fileName + " :: " + e.getMessage());
            responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        } */catch (Exception ex) {
            log.error("Error while processing file " + fileName + " :: " + ex.getMessage());
            responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        }
        return candidateList;
    }
}



