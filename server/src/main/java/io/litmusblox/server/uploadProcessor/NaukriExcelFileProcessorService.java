/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.User;
import io.litmusblox.server.service.UploadResponseBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
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
    public List<Candidate> process(String fileName, UploadResponseBean responseBean, boolean ignoreMobile, String repoLocation, User loggedInUser) {
        List<Candidate> candidateList = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        try {
            DataFormatter dataFormatter = new DataFormatter();
            HashMap<String, Integer> columnPositionMap = new HashMap<>();
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
            Map<String, ArrayList<String>> naukriColumnsMap = IConstant.NAUKRI_FILE_COLUMNS_MAP;
            boolean downloadedFlag = false;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

//                log.info("Inside row "+row.getRowNum());
//                log.info("Number of cells in this row are: "+row.getPhysicalNumberOfCells());
                if (row.getRowNum() <= 1) {
                    if(row.getRowNum()==0 && row.getCell(0).getStringCellValue().toLowerCase().contains("downloaded")) {
                        log.info("'Downloaded' exists. Skipping first two rows.");
                        downloadedFlag=true;
                    }
                    if(downloadedFlag) {
                        continue; //skip first 2 rows
                    }
                }

                if(headingRow) {
                    //Check the Header Column Names with the IConstant.NaukriFileHeader names and map them
                    if(row.getPhysicalNumberOfCells()!=0){
                        String[] naukriFileHeadersArray = new String[row.getPhysicalNumberOfCells()];
                        Iterator<Cell> cellIterator = row.cellIterator();
                        while(cellIterator.hasNext()) {
//                            log.info("Inside cell");
                            Cell cell = cellIterator.next();
                            String cellValue = dataFormatter.formatCellValue(cell);
                            int cellIndex = cell.getColumnIndex();
                            naukriFileHeadersArray[cellIndex] = cellValue;
                        }
                        for(String naukriColumn:naukriColumnsMap.keySet()){

                            columnPositionMap.putIfAbsent(naukriColumn,-1);
//                            log.info(naukriColumn+" has been initialized");
                        }

                        for(int arrayIndex=row.getFirstCellNum();arrayIndex<naukriFileHeadersArray.length;arrayIndex++){
                            int finalArrayIndex = arrayIndex;
                            String naukriFileHeaderValue = naukriFileHeadersArray[finalArrayIndex];

                            for(String naukriColumn:naukriColumnsMap.keySet()){
                                for(String headerName:naukriColumnsMap.get(naukriColumn)){
                                    if (headerName.equalsIgnoreCase(naukriFileHeaderValue)) {
                                        columnPositionMap.compute(naukriColumn, (key, value) -> value = finalArrayIndex);
                                        break;
                                    }
                                }
                            }
                        }

                        //for removing non existent key-value pairs
                        log.info("Working on removal of non existent keys");
                        log.info("The original size of columnPositionMap is:  {}", columnPositionMap.size());
                        Set<String> mapKeySet = columnPositionMap.keySet();
                        Iterator<String> mapIterator = mapKeySet.iterator();
                        while(mapIterator.hasNext()){
                            String mapKey = mapIterator.next();
                            if(columnPositionMap.get(mapKey)<0){
                                mapIterator.remove();
                                log.info("removed {}",mapKey);
                            }
                        }
                        log.info("Number of matching headers present in Excel are: {} ",columnPositionMap.size());
                    }

                    else if(row.getPhysicalNumberOfCells()!=0 || columnPositionMap.isEmpty()){
                        throw new WebException(IErrorMessages.MISSING_COLUMN_NAMES_FIRST_ROW, HttpStatus.UNPROCESSABLE_ENTITY);
                    }

                    log.info("Initialized Array and Map ");
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
                    while (cellIterator.hasNext() && index < columnPositionMap.size()) {
                        Cell cell = cellIterator.next();
                        String cellValue = dataFormatter.formatCellValue(cell);
                        String naukriColumn;
                        if (Util.isNotNull(cellValue) && discardRow) {
                            discardRow = false;
                            naukriRow = new NaukriFileRow();
                        }
                        for(String mapKey:columnPositionMap.keySet()){
                            if(columnPositionMap.get(mapKey)==index){
                                naukriColumn = mapKey;
                                naukriRow.getClass().getField(naukriColumn).set(naukriRow, cellValue.trim());
                                log.info("Naukri Row filled ");
                            }
                        }
                        index++;
                        log.info(cellValue);
                    }
                    if (!discardRow) {
                        log.info("Candidate creation started ");
                        Candidate candidate = new Candidate();
                        candidate.setCandidateSource(IConstant.CandidateSource.File.getValue());
                        convertNaukriRowToCandidate(candidate, naukriRow);
                        candidateList.add(candidate);
                    }
                }
            }
            log.info("End reached");
        } catch(WebException we) {
            log.error("Error while parsing file " + fileName + " :: " + we.getMessage());
            throw we;
        } catch(IOException ioe) {
            log.error("Error while parsing file " + fileName + " :: " + ioe.getMessage());
            log.info("Trying to parse as an html file now. Specific handling for Naukri xls files.");
            return new HTMLFileProcessorService().process(fileName, responseBean, ignoreMobile, repoLocation, loggedInUser);
        } catch (InvalidFormatException e) {
            log.error("Error while parsing file " + fileName + " :: " + e.getMessage());
            responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        } catch (Exception ex) {
            log.error("Error while processing file " + fileName + " :: " + ex.getMessage());
            responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        }
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime-startTime;
        log.info("Naukri File Processing time {}ms", timeTaken);
        return candidateList;
    }
}
