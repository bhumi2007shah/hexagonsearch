/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.*;
import io.litmusblox.server.uploadProcessor.HtmlParser;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author : Shital Raval
 * Date : 2/12/19
 * Time : 11:49 AM
 * Class Name : NaukriHtmlParser
 * Project Name : server
 */
@Log4j2
@Service
public class NaukriHtmlParser implements HtmlParser {
    @Override
    public Candidate parseData(String htmlData, User createdBy) {
        log.info("Parsing html data from Naukri email");
        Candidate candidateFromNaukriEmail = Candidate.builder().
                candidateSource(IConstant.CandidateSource.NaukriJobPosting.getValue()).
                createdOn(new Date()).
                createdBy(createdBy).build();

        Document doc = Jsoup.parse(htmlData);
        populateCandidateName(candidateFromNaukriEmail, doc);
        populateEmail(candidateFromNaukriEmail, doc.select("a"));
        populateMobile(candidateFromNaukriEmail, doc);

        try {
            candidateFromNaukriEmail.setCandidateCompanyDetails(new ArrayList<CandidateCompanyDetails>(1));
        } catch (Exception e) {
            log.error("Error when populating candidate company details {}", e.getMessage());
            log.info(Util.getStackTrace(e));
        }
        try {
            populateCandidateCompanyDetails(candidateFromNaukriEmail, doc);
        } catch (Exception e) {
            log.error("Error when populating candidate company details {}", e.getMessage());
            log.info(Util.getStackTrace(e));
        }

        try {
            populateCandidateDetails(candidateFromNaukriEmail, doc);
        } catch (Exception e) {
            log.error("Error when populating candidate details {}", e.getMessage());
            log.info(Util.getStackTrace(e));
        }
        //before at is designation, after at is current company
        log.info("Completed processing html");
        return candidateFromNaukriEmail;
    }

    private void populateCandidateCompanyDetails(Candidate candidate, Document doc) {
        if(null != doc.getElementsByAttributeValueContaining("src", "rating-icon.png").first().parent().parent().nextElementSibling().nextElementSibling().text()) {
            String text = doc.getElementsByAttributeValueContaining("src", "rating-icon.png").first().parent().parent().nextElementSibling().nextElementSibling().text();
            if( null !=text && !text.contains("Not Mentioned")) {
                CandidateCompanyDetails companyDetails = CandidateCompanyDetails.builder().designation(text.substring(0, text.indexOf(" at "))).companyName(text.substring(text.indexOf(" at ") + 4)).build();
                candidate.getCandidateCompanyDetails().add(companyDetails);
            }
            else{
                CandidateCompanyDetails companyDetails = CandidateCompanyDetails.builder().designation(null).companyName("Not Mentioned").build();
                candidate.getCandidateCompanyDetails().add(companyDetails);
            }
        }
    }

    private void populateCandidateName(Candidate candidate, Document doc) {
        candidate.setCandidateName(doc.getElementsByAttributeValueContaining("src", "rating-icon.png").first().parent().text());
    }

    private void populateEmail(Candidate candidate, Elements links) {
        final String[] email = new String[1];
        links.stream().forEach(link-> {
            if (null != link.attributes() && link.attributes().asList().size() > 0) {
                if (link.attributes().asList().get(0).getValue().indexOf("mailto:") > -1) {
                    if (!"support@naukri.com".equals(link.attributes().asList().get(0).getValue().substring(link.attributes().asList().get(0).getValue().indexOf("mailto:") + 7))) {
                        email[0] = link.attributes().asList().get(0).getValue().substring(link.attributes().asList().get(0).getValue().indexOf("mailto:") + 7);
                        candidate.setEmail(email[0]);
                        log.info("found email: {}", email[0]);
                    }
                }
            }
        });
    }

    private void populateMobile(Candidate candidate, Document doc){
        String mobile = "";
        if(null != doc.getElementsByAttributeValueContaining("src", "call-1-icon1.png")){
            mobile = doc.getElementsByAttributeValueContaining("src", "call-1-icon1.png").first().parent().text();
            log.info("found mobile {}", mobile);
            candidate.setMobile(mobile);
        }
    }

    private void populateCandidateDetails(Candidate candidate, Document doc) {
        String experience = doc.getElementsByAttributeValueContaining("src", "exp-icon1.png").first().parent().text();

        if(experience.indexOf("Not Mentioned") == -1 && experience.indexOf("Fresher") == -1) {
            //set experience
            String years = null;
            String months = null;
            if(experience.contains("&")) {
                years = experience.substring(0, experience.indexOf("Years &")).trim();
                months = experience.substring(experience.indexOf("Years &") + 7, experience.indexOf("Months")).trim();
                candidate.setCandidateDetails(CandidateDetails.builder().totalExperience(Double.parseDouble(years + "." + months)).build());
            }
            else{
                years = experience.substring(0, experience.indexOf("Year"));
                candidate.setCandidateDetails(CandidateDetails.builder().totalExperience(Double.parseDouble(years)).build());
            }

        }

        String ctc = doc.getElementsByAttributeValueContaining("src", "pckg-icon1.png").first().parent().text();
        if(null!=ctc){
            //set ctc
            if (candidate.getCandidateCompanyDetails().size() == 0)
                candidate.getCandidateCompanyDetails().add(CandidateCompanyDetails.builder().salary(ctc.trim()).build());
            else
                candidate.getCandidateCompanyDetails().get(0).setSalary(ctc.trim());
        }

        log.info("Set experience and ctc");

        //set noticePeriod, code commented as new html of naukri doesn't have notice period
        /*
        String noticePeriod = doc.getElementsMatchingOwnText("Notice Period").last().parents().tagName("tbody").get(1).text().replaceAll("Notice Period","").trim();
        if (candidate.getCandidateCompanyDetails().size() == 0)
            candidate.getCandidateCompanyDetails().add(new CandidateCompanyDetails());

        if (noticePeriod.indexOf("Not Mentioned") != -1 || noticePeriod.indexOf("Serving") != -1) {
            log.info("Candidate notice period is Not mentioned or candidate is serving notice period");
            candidate.getCandidateCompanyDetails().get(0).setNoticePeriod("0");
        }
        else if (noticePeriod.indexOf("Months") != -1)
            candidate.getCandidateCompanyDetails().get(0).setNoticePeriod(String.valueOf(Integer.parseInt(noticePeriod.substring(0, noticePeriod.indexOf("Months")).trim()) * 30));
        else
            candidate.getCandidateCompanyDetails().get(0).setNoticePeriod(String.valueOf(Integer.parseInt(noticePeriod.substring(0, noticePeriod.indexOf("Days")).trim())));
        */

        //set education
        String education = doc.getElementsContainingOwnText("Education").first().parent().getElementsContainingOwnText(" at ").text();
        if (null != education && !education.isEmpty() && !education.isBlank()) {
            if(null == candidate.getCandidateEducationDetails())
                candidate.setCandidateEducationDetails(new ArrayList<CandidateEducationDetails>());
            if(education.indexOf("at") == -1)
                candidate.getCandidateEducationDetails().add(CandidateEducationDetails.builder().degree(education.substring(0, education.indexOf(" at"))).instituteName(null).build());
            else
                candidate.getCandidateEducationDetails().add(CandidateEducationDetails.builder().degree(education.substring(0, education.indexOf("at")).trim()).instituteName(education.substring(education.indexOf("at") + 2).trim()).build());
        }

        //set location
        String location = doc.getElementsContainingOwnText("Location").next().next().text();
                //doc.getElementsMatchingOwnText("Location").first().parent().tagName("tr").nextElementSibling().nextElementSibling().text();
                //parents().tagName("tr").first().nextElementSibling().nextElementSibling().text();

        if(location.indexOf("(") !=-1) {
            if(null == candidate.getCandidateDetails())
                candidate.setCandidateDetails(new CandidateDetails());
            candidate.getCandidateDetails().setLocation(location.substring(0, location.indexOf("(")).trim());
        }

        //set keyskill
        String keyskills = doc.getElementsMatchingOwnText("Keyskills").next().next().text();
        if (null != keyskills) {
            String[] allSkills = keyskills.split(",");
            List<CandidateSkillDetails> candidateSkillDetails = new ArrayList<>();
            Arrays.stream(allSkills).forEach(skill-> candidateSkillDetails.add(CandidateSkillDetails.builder().skill(skill).build()));
            candidate.setCandidateSkillDetails(candidateSkillDetails);
        }
    }
}
