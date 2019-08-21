/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.service.UploadResponseBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author : Sumit
 * Date : 19/7/19
 * Time : 4:40 PM
 * Class Name : NaukriExcelFileProcessorService
 * Project Name : server
 */
@Log4j2
public class NaukriExcelFileProcessorService extends AbstractNaukriProcessor implements IUploadFileProcessorService {

    private static final String XLSX = "xlsx";
    @Override
    public List<Candidate> process(String fileName, UploadResponseBean responseBean, boolean ignoreMobile, String repoLocation) {
        List<Candidate> candidateList = new ArrayList<>();
        try {
            DataFormatter dataFormatter = new DataFormatter();

            // Finds the workbook instance for XLSX file
            Workbook myWorkBook;
            try {
                log.info("Trying to make a workbook in try...");
                myWorkBook = WorkbookFactory.create(new File(repoLocation + File.separator + fileName));
            } catch (IOException invalidFormatException) {
                //naukri xlsx files do not have content type, so poi doesn't recognize the file.
                //add the content type.
                //for xls files generated by naukri, it is actually an html file, hand over to html processor
                if(fileName.endsWith(XLSX)) {
                    log.info("Modifying file, adding missing content type to the file, so that poi can process it.");
                    Util.modifyTextFileInZip(repoLocation + File.separator + fileName);
                    // Thread.sleep(10000);
                    myWorkBook = new XSSFWorkbook(new File(repoLocation + File.separator + fileName));
                }
                else
                    throw invalidFormatException;
            }
            // Return first sheet from the XLSX workbook
            Sheet mySheet = myWorkBook.getSheetAt(0);

            // Get iterator to all the rows in current sheet
            Iterator<Row> rowIterator = mySheet.rowIterator();//iterator();

            log.info("no. of rows in sheet is " + mySheet.getLastRowNum());

            // Traversing over each row of XLSX file
            boolean headingRow = true;
            IConstant.NAUKRI_FILE_COLUMNS[] naukriColumns = IConstant.NAUKRI_FILE_COLUMNS.values();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                if (row.getRowNum() <= 1)
                    continue; //skip first 2 rows

                if(headingRow) {
                    try {
                        //check that the row contains column names in correct order
                        if ((row.getPhysicalNumberOfCells() == 0) || !checkForCells(row)){
                            Map<String, String> breadCrumb = new HashMap<>();
                            breadCrumb.put(IConstant.UPLOAD_FILE_TYPE,IConstant.PROCESS_FILE_TYPE.ExcelFile.toString());
                            breadCrumb.put("Row name", row.toString());
                            Util.sendSentryErrorMail(fileName, breadCrumb, IConstant.PROCESS_FILE_TYPE.NaukriExcelFile.toString());
                            throw new WebException(IErrorMessages.MISSING_COLUMN_NAMES_FIRST_ROW, HttpStatus.UNPROCESSABLE_ENTITY);
                        }
                    } catch (Exception e) {
                        throw new WebException(IErrorMessages.MISSING_COLUMN_NAMES_FIRST_ROW, HttpStatus.UNPROCESSABLE_ENTITY);
                    }
                    headingRow = false;
                    continue;
                }

                //check if the row is empty
                if(row.getPhysicalNumberOfCells() > 0) {

                    // For each row, iterate through each columns
                    Iterator<Cell> cellIterator = row.cellIterator();
                    int index = 0;
                    boolean discardRow = true;
                    NaukriFileRow naukriRow = null;
                    while (cellIterator.hasNext() && index < naukriColumns.length) {
                        Cell cell = cellIterator.next();
                        String cellValue = dataFormatter.formatCellValue(cell);
                        if (Util.isNotNull(cellValue) && discardRow) {
                            discardRow = false;
                            naukriRow = new NaukriFileRow();
                        }
                        naukriRow.getClass().getField(naukriColumns[index].name()).set(naukriRow, cellValue.trim());
                        index++;
                    }
                    if (!discardRow) {
                        Candidate candidate = new Candidate();
                        candidate.setCandidateSource(IConstant.CandidateSource.File.getValue());
                        convertNaukriRowToCandidate(candidate, naukriRow);
                        candidateList.add(candidate);
                    }
                }
            }
        } catch(WebException we) {
            log.error("Error while parsing file " + fileName + " :: " + we.getMessage());
            throw we;
        } catch(IOException ioe) {
            log.error("Error while parsing file " + fileName + " :: " + ioe.getMessage());
            log.info("Trying to parse as an html file now. Specific handling for Naukri xls files.");
            return new HTMLFileProcessorService().process(fileName, responseBean, ignoreMobile, repoLocation);
        } catch (InvalidFormatException e) {
            log.error("Error while parsing file " + fileName + " :: " + e.getMessage());
            responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        } catch (Exception ex) {
            log.error("Error while processing file " + fileName + " :: " + ex.getMessage());
            responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        }
        return candidateList;
    }
}
