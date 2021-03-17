/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.*;
import io.litmusblox.server.service.IDetectTextInImage;
import io.litmusblox.server.uploadProcessor.HtmlParser;
import io.litmusblox.server.utils.SentryUtil;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author : sameer
 * Date : 23/01/20
 * Time : 4:44 PM
 * Class Name : NaukriMassMailParser
 * Project Name : server
 */
@Log4j2
@Service
public class NaukriMassMailParser implements HtmlParser {

    @Autowired
    IDetectTextInImage detectTextInImage;

    /**
     * Function creates a Candidate and set name, email, mobile, candidate detail and candidate company details
     * @param htmlData - Email body
     * @param createdBy - Object of user who created the job for which this email is being proecssed
     * @return Candidate with data from email body
     */
    @Override
    public Candidate parseData(String htmlData, User createdBy) {
        long startTime = System.currentTimeMillis();
        log.info("Parsing HTML data from Naukri mass mail email");
        Candidate candidateFromNaukriEmail = Candidate.builder().
                candidateSource(IConstant.CandidateSource.NaukriMassMail.getValue()).
                createdOn(new Date()).
                createdBy(createdBy).build();

        Document doc = Jsoup.parse(htmlData);

        //calling function to populate canididate name
        try {
            populateCandidateName(candidateFromNaukriEmail, doc);
        }
        catch (Exception e){
            log.error("error while processing candidate name {}", e.getMessage());
            SentryUtil.logWithStaticAPI(null, "error while processing candidate name, "+e.getMessage(), null);
            e.printStackTrace();
        }

        // calling function to populate candidate's email and mobile
        try {
            populateMobileAndEmail(candidateFromNaukriEmail, doc);
        }
        catch (Exception e){
            log.error("error while processing candidate mobile and email {}", e.getMessage());
            SentryUtil.logWithStaticAPI(null, "error while processing candidate email and mobile, "+e.getMessage(), null);
            e.printStackTrace();
        }

        //Initializing company detail with empty list and calling function to populate candidate company details

        candidateFromNaukriEmail.setCandidateCompanyDetails(new ArrayList<CandidateCompanyDetails>(1));

        try {
            populateCandidateCompanyDetails(candidateFromNaukriEmail, doc);
        } catch (Exception e) {
            log.error("Error when populating candidate company details {}", e.getMessage());
            SentryUtil.logWithStaticAPI(null, "error while processing candidate company details "+e.getMessage(), null);
            e.printStackTrace();
        }

        //calling function to populate candidate details
        try {
            populateCandidateDetails(candidateFromNaukriEmail, doc);
        } catch (Exception e) {
            log.error("Error when populating candidate details {}", e.getMessage());
            SentryUtil.logWithStaticAPI(null, "error while processing candidate details, "+e.getMessage(), null);
            e.printStackTrace();
        }
        //before at is designation, after at is current company
        log.info("Completed processing html in {} ms", (System.currentTimeMillis()-startTime));

        return candidateFromNaukriEmail;
    }

    /**
     * Function to populate candidateName in candidate object passed as param using email body
     * @param candidate
     * @param doc
     */
    private void populateCandidateName(Candidate candidate, Document doc) {
        log.info("Extacting candidate name");
        if(doc.getElementsByAttributeValueContaining("src", "UserPic").size()!=0){
            if(doc.getElementsByAttributeValueContaining("src", "UserPic").get(0).parent().parent().parent().parent().parent().previousSibling().previousSibling().childNode(1).childNode(1).childNode(0).childNode(1).childNode(1).childNode(0).toString()!=null) {
                candidate.setCandidateName(doc.getElementsByAttributeValueContaining("src", "UserPic").get(0).parent().parent().parent().parent().parent().previousSibling().previousSibling().childNode(1).childNode(1).childNode(0).childNode(1).childNode(1).childNode(0).toString().trim());
            }
        }
        else{
            if(doc.getElementsByAttributeValueContaining("style", "font-size:22px").text()!=null){
                candidate.setCandidateName(doc.getElementsByAttributeValueContaining("style", "font-size:22px").text());
            }
        }
        log.info("Found candidate name: {} ", candidate.getCandidateName());
    }

    /**
     * Function to set email and mobile in candidate object using doc
     * @param candidate
     * @param doc
     */
    private void populateMobileAndEmail(Candidate candidate, Document doc) {
        candidate.setMobile(extractMobile(doc));
        candidate.setEmail(extractEmail(doc));
    }

    /**
     * Function to extract mobile from doc
     * @param doc
     * @return - String - mobile
     */
    private String extractMobile(Document doc) {
        log.info("extracting mobile number");
        //find mobile number element
        Elements mobileContainer = doc.getElementsContainingOwnText("mobile");
        String mobile = null;
        // mobile is an image, converting it to text using google's API
        try {
            URL mobileImageUrl = new URL(String.valueOf(mobileContainer.get(0).getElementsByTag("img").attr("src")));
            mobile = detectTextInImage.detectText(mobileImageUrl);
        }
        catch(MalformedURLException e) {
            log.error("Malformed URL for mobile image, {}", e.getMessage());
        } catch (Exception e) {
            log.error(" Error while converting image to text, {}", e.getMessage());
        }
        log.info("Found mobile number {}", mobile);
        return mobile;
    }

    /**
     * Function to extract email from doc
     * @param doc
     * @return - String - email
     */
    private String extractEmail(Document doc){
        String email = null;
        /*
        if(doc.getElementsByClass("aSH")!=null) {
            for(Node node: doc.getElementsByClass("aSH")){
            }
        }*/
        //returning null email as candidate's email is not present in massMail email.
        return email;
    }

    /**
     * Function to populate candidate company details with designation, company, notice period, salary
     * @param candidate
     * @param doc
     * @throws Exception
     */
    private void populateCandidateCompanyDetails(Candidate candidate, Document doc) throws Exception {
        CandidateCompanyDetails candidateCompanyDetails = new CandidateCompanyDetails();

        log.info("setting current designation");
        if(doc.getElementsContainingOwnText("Current Designation").next().text()!=null){
            log.info("Found company details for candidate: {}, {}", candidate.getCandidateName(), candidate.getMobile());
            candidateCompanyDetails.setDesignation(
                    doc.getElementsContainingOwnText("Current Designation").next().text()
            );
        }
        log.info("set current designation");

        log.info("setting current company");
        if(doc.getElementsContainingOwnText("Current Company").next().text()!=null) {
            candidateCompanyDetails.setCompanyName(
                    doc.getElementsContainingOwnText("Current Company").next().text()
            );
        }
        log.info("set current company");

        log.info("setting notice period");
        if(doc.getElementsContainingOwnText("Notice Period").next().text()!=null || !doc.getElementsContainingOwnText("Notice Period").next().text().equals("Not Mentioned")) {
            if (doc.getElementsContainingOwnText("Notice Period").next().text().contains("Months")) {
                candidateCompanyDetails.setNoticePeriod(String.valueOf(Integer.parseInt(
                        doc.getElementsContainingOwnText("Notice Period").next().text().substring(
                                0, doc.getElementsContainingOwnText("Notice Period").next().text().indexOf("Months")
                        ).trim()) * 30));
            } else {
                candidateCompanyDetails.setNoticePeriod(String.valueOf(Integer.parseInt(
                        doc.getElementsContainingOwnText("Notice Period").next().text().substring(
                                0, doc.getElementsContainingOwnText("Notice Period").next().text().indexOf("Days")
                        ).trim())));
            }
        }
        else{
            candidateCompanyDetails.setNoticePeriod("0");
        }
        log.info("set notice period");

        log.info("setting ctc");
        //set ctc
        if(
                doc.getElementsContainingOwnText("CTC").get(0).parent().parent().nextElementSibling().childNode(1)
                        .childNode(1).childNode(1).attr("src")!=null
        ) {
            String ctc = "";
            URL ctcUrl = new URL(doc.getElementsContainingOwnText("CTC")
                    .get(0)
                    .parent()
                    .parent()
                    .nextElementSibling()
                    .childNode(1)
                    .childNode(1)
                    .childNode(1).attr("src")
            );

            ctc = detectTextInImage.detectText(ctcUrl);
            if(ctc!="Not Mentioned")
                candidateCompanyDetails.setSalary(ctc.replaceAll("((?:\\D)(?!\\d))", ""));
        }
        log.info("set ctc");

        candidate.getCandidateCompanyDetails().add(candidateCompanyDetails);
    }

    /**
     * Function to populate candidate details with experience, location, preferred location, education
     * @param candidate
     * @param doc
     * @throws Exception
     */
    public void populateCandidateDetails(Candidate candidate, Document doc) throws Exception {
        log.info("setting experince");
        //set experience
        doc.getElementsContainingOwnText("Exp").forEach(element -> {
            if(element.text().equals("Exp") && element.parent().parent().nextElementSibling().childNode(1)
                    .childNode(1).childNode(0).toString().trim()!=null){
                String experience = element
                        .parent()
                        .parent()
                        .nextElementSibling()
                        .childNode(1)
                        .childNode(1)
                        .childNode(0)
                        .toString().trim();
                if(!experience.equals("Not Mentioned")) {
                    String years = "";
                    String months = "";
                    if(experience.contains("Yrs")) {
                        years = experience.substring(0, experience.indexOf("Yrs")).trim();
                    }
                    if(experience.contains("M")) {
                        months = experience.substring((experience.indexOf("Yrs") + 3), experience.indexOf("M")).trim();
                    }
                    candidate.setCandidateDetails(CandidateDetails.builder().totalExperience(Double.parseDouble(years + "." + months)).build());
                }
            }
        });
        log.info("set experince");


        log.info("setting location");
        //set location
        if(
                doc.getElementsContainingOwnText("Location").get(0).parent().parent().nextElementSibling().childNode(1)
                        .childNode(1).childNode(1).attr("src")!=null
        ) {
            String location = "";
            URL locationUrl = new URL(doc.getElementsContainingOwnText("Location")
                    .get(0)
                    .parent()
                    .parent()
                    .nextElementSibling()
                    .childNode(1)
                    .childNode(1)
                    .childNode(1).attr("src")
            );

            location = detectTextInImage.detectText(locationUrl);
            candidate.getCandidateDetails().setLocation(location);
        }
        log.info("set location");

        log.info("setting preferred location");
        //set preferred location
        if(doc.getElementsContainingOwnText("Preferred Location")!=null){
            if(!doc.getElementsContainingOwnText("Preferred Location").get(0).parent().text().split(":")[1].trim().equals("Not Mentioned")){
                candidate.getCandidateDetails().setPreferredLocations(
                        doc.getElementsContainingOwnText("Preferred Location").get(0).parent().text().split(":")[1].trim()
                );
            }
        }
        log.info("set preferred location");

        log.info("setting candidate education details");
        //set education
        if(doc.getElementsContainingOwnText("Education")!=null){
            if(doc.getElementsContainingOwnText("Education").get(0).parent().nextElementSibling()!=null && doc.getElementsContainingOwnText("Education").get(0).parent().nextElementSibling().nextElementSibling()!=null){

                log.info("Found candidate education for candidate : {}, {}", candidate.getCandidateName(), candidate.getMobile());

                if(null == candidate.getCandidateEducationDetails())
                    candidate.setCandidateEducationDetails(new ArrayList<CandidateEducationDetails>());

                candidate.getCandidateEducationDetails().add(CandidateEducationDetails.builder()
                        .degree(doc.getElementsContainingOwnText("Education").get(0).parent().nextElementSibling().text())
                        .instituteName(doc.getElementsContainingOwnText("Education").get(0).parent().nextElementSibling().nextElementSibling().text())
                        .build()
                );

            }
        }
        log.info("set candidate education details");
    }
}
