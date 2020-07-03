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
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.*;


/**
 * @author : Sumit
 * Date : 19/7/19
 * Time : 4:30 PM
 * Class Name : HTMLFileProcessorService
 * Project Name : server
 */
@Log4j2
public class HTMLFileProcessorService extends AbstractNaukriProcessor implements IUploadFileProcessorService {

    @Override
    public List<Candidate> process(String fileName, UploadResponseBean responseBean, boolean ignoreMobile, String repoLocation, User loggedInUser, String fileType) {
        List<Candidate> candidateList = new ArrayList<>(0);
        try {
            Document doc = Jsoup.parse(new File(repoLocation + File.separator + fileName), "utf-8");

            Element body = doc.body();
            Elements trElements = body.select("tr");

            int count = 0;
            for (Element trElement: trElements
            ) {
                if(count++ <= 1)
                    continue;

                //process the first tr element as headings
                switch(count) {
                    case 3:
                        //processing headers
                        if (!processHeaders(trElement)){
                            Map<String, String> breadCrumb = new HashMap<>();
                            breadCrumb.put("File Name", fileName);
                            breadCrumb.put("File Type", IConstant.PROCESS_FILE_TYPE.HTMLFile.toString());
                            breadCrumb.put("trElement", trElement.toString());
                            throw new WebException(IConstant.UPLOAD_FORMATS_SUPPORTED.Naukri.toString() + IErrorMessages.MISSING_COLUMN_NAMES_FIRST_ROW, HttpStatus.INTERNAL_SERVER_ERROR.UNPROCESSABLE_ENTITY, breadCrumb);
                        }
                        break;
                    default:
                        NaukriFileRow naukriRow = createNaukriRow(trElement);
                        Candidate candidate = new Candidate();
                        candidate.setCandidateSource(IConstant.CandidateSource.File.getValue());
                        convertNaukriRowToCandidate(candidate, naukriRow);
                        candidateList.add(candidate);
                        break;
                }
            }
            return candidateList;
        } catch (Exception e) {
            responseBean.setStatus(IConstant.UPLOAD_STATUS.Failure.name());
        }
        return null;
    }

    //verify the column names
    private boolean processHeaders(Element element) {
        //check for correct sequence
        IConstant.NAUKRI_XLS_FILE_COLUMNS[] fileColumns = IConstant.NAUKRI_XLS_FILE_COLUMNS.values();
        for (int i = 0; i < fileColumns.length; i++) {
            if(null == element.getElementsByIndexEquals(i)|| !(element.getElementsByIndexEquals(0).get(i+1).ownText().equalsIgnoreCase(fileColumns[i].getValue()))) {
                StringBuffer logMsg = new StringBuffer("Naukri xls file error: Constant value = ").append(fileColumns[i].getValue()).append("Value from file = ").append(element.getElementsByIndexEquals(0).get(i+1).ownText());

                Map<String, String> breadCrumb = new HashMap<>();
                breadCrumb.put("Naukri XLS constant value ", fileColumns[i].getValue());
                breadCrumb.put("Header Value in uploaded file ", element.getElementsByIndexEquals(0).get(i+1).ownText());
                //SentryUtil.logWithStaticAPI(null, "Naukri xls file parsing error", breadCrumb);
                log.error(logMsg.toString());

                return false;
            }
        }
        return true;
    }

    private NaukriFileRow createNaukriRow(Element element) throws Exception {
        NaukriFileRow naukriRow = new NaukriFileRow();

        Iterator<Element> elementIterator = element.getAllElements().first().getAllElements().iterator();
        IConstant.NAUKRI_FILE_COLUMNS[] fileColumns = IConstant.NAUKRI_FILE_COLUMNS.values();
        int i = 0;
        while(elementIterator.hasNext() && i < fileColumns.length) {
            if (i == 0 || i == 2)
                elementIterator.next();

            String cellValue = elementIterator.next().ownText();
            naukriRow.getClass().getField(fileColumns[i].name()).set(naukriRow, cellValue.trim());

            i++;
        }
        return naukriRow;
    }
}
