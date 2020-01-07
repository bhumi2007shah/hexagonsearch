/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.*;
import io.litmusblox.server.uploadProcessor.HtmlParser;
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
                candidateSource(IConstant.CandidateSource.NaukriEmail.getValue()).
                createdOn(new Date()).
                createdBy(createdBy).build();

        Document doc = Jsoup.parse(htmlData);
        populateCandidateName(candidateFromNaukriEmail, doc);
        populateMobileAndEmail(candidateFromNaukriEmail, doc.select("a"));

        try {
            candidateFromNaukriEmail.setCandidateCompanyDetails(new ArrayList<CandidateCompanyDetails>(1));
        } catch (Exception e) {
            log.error("Error when populating candidate company details {}", e.getMessage());
            e.printStackTrace();
        }
        try {
            populateCandidateCompanyDetails(candidateFromNaukriEmail, doc.getElementsContainingOwnText(" at ").text());
        } catch (Exception e) {
            log.error("Error when populating candidate company details {}", e.getMessage());
            e.printStackTrace();
        }

        try {
            populateCandidateDetails(candidateFromNaukriEmail, doc);
        } catch (Exception e) {
            log.error("Error when populating candidate company details {}", e.getMessage());
            e.printStackTrace();
        }
        //before at is designation, after at is current company
        log.info("Completed processing html");
        return candidateFromNaukriEmail;
    }

    private void populateCandidateCompanyDetails(Candidate candidate, String text) {
        if(text.length() > 0 && text.indexOf(" at ") != -1) {
            CandidateCompanyDetails companyDetails = CandidateCompanyDetails.builder().designation(text.substring(0, text.indexOf(" at "))).companyName(text.substring(text.indexOf(" at ") + 4)).build();
            candidate.getCandidateCompanyDetails().add(companyDetails);
        }
    }

    private void populateCandidateName(Candidate candidate, Document doc) {
        candidate.setCandidateName(doc.getElementsContainingOwnText(" has applied to your job").first().ownText().substring(0,doc.getElementsContainingOwnText(" has applied to your job").first().ownText().indexOf("has applied to your job")));
    }

    private void populateMobileAndEmail(Candidate candidate, Elements links) {
        final String[] mobile = new String[1];
        final String[] email = new String[1];
        links.stream().forEach(link-> {
            if(link.attributes().asList().get(0).getValue().indexOf("tel:") > -1 && candidate.getMobile() == null) {
                mobile[0] = link.attributes().asList().get(0).getValue().substring(link.attributes().asList().get(0).getValue().indexOf("tel:")+4);
                candidate.setMobile(mobile[0]);
                log.info("found mobile: {}", mobile[0]);
            }
            else if(link.attributes().asList().get(0).getValue().indexOf("mailto:") > -1) {
                if(!"support@naukri.com".equals(link.attributes().asList().get(0).getValue().substring(link.attributes().asList().get(0).getValue().indexOf("mailto:")+7))) {
                    email[0] = link.attributes().asList().get(0).getValue().substring(link.attributes().asList().get(0).getValue().indexOf("mailto:") + 7);
                    candidate.setEmail(email[0]);
                    log.info("found email: {}", email[0]);
                }
            }
        });
    }

    private void populateCandidateDetails(Candidate candidate, Document doc) {
        String experienceAndCtc = doc.getElementsMatchingOwnText("Total Experience").parents().tagName("tbody").get(1).text().replaceAll("Total Experience CTC ","").replaceAll("&", "");

        if(experienceAndCtc.indexOf("Not Mentioned") == -1 && experienceAndCtc.indexOf("Fresher") == -1) {
            //set experience
            String experience = experienceAndCtc.substring(0, experienceAndCtc.indexOf("Months"));
            String years = experience.substring(0, experienceAndCtc.indexOf("Years")).trim();
            String months = experience.substring(experienceAndCtc.indexOf("Years") + 5).trim();
            candidate.setCandidateDetails(CandidateDetails.builder().totalExperience(Double.parseDouble(years + "." + months)).build());

            //set ctc
            if (candidate.getCandidateCompanyDetails().size() == 0)
                candidate.getCandidateCompanyDetails().add(CandidateCompanyDetails.builder().salary(experienceAndCtc.substring(experienceAndCtc.indexOf("Months") + 6).trim()).build());
            else
                candidate.getCandidateCompanyDetails().get(0).setSalary(experienceAndCtc.substring(experienceAndCtc.indexOf("Months") + 6).trim());
        }
        log.info("Set experience");
        //set noticePeriod
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

        //set education
        String education = doc.getElementsMatchingOwnText("UG").parents().tagName("tbody").get(1).text();
        String pgEduction = education.substring(education.indexOf("PG") + 5);
        if (null != pgEduction) {
            if(null == candidate.getCandidateEducationDetails())
                candidate.setCandidateEducationDetails(new ArrayList<CandidateEducationDetails>());
            if (pgEduction.indexOf("from") != -1)
                candidate.getCandidateEducationDetails().add(CandidateEducationDetails.builder().degree(pgEduction.substring(0, pgEduction.indexOf("from")).trim()).instituteName(pgEduction.substring(pgEduction.indexOf("from") + 4).trim()).build());
        }
        String ugEducation = education.substring(education.indexOf("UG") + 5, education.indexOf("PG"));
        if (null != ugEducation) {
            if(null == candidate.getCandidateEducationDetails())
                candidate.setCandidateEducationDetails(new ArrayList<CandidateEducationDetails>());
            if(ugEducation.indexOf("from") == -1)
                candidate.getCandidateEducationDetails().add(CandidateEducationDetails.builder().degree(ugEducation).instituteName(ugEducation.substring(ugEducation.indexOf("from") + 4).trim()).build());
            else
                candidate.getCandidateEducationDetails().add(CandidateEducationDetails.builder().degree(ugEducation.substring(0, ugEducation.indexOf("from")).trim()).instituteName(ugEducation.substring(ugEducation.indexOf("from") + 4).trim()).build());
        }

        //set location
        String location = doc.getElementsMatchingOwnText("Location").first().parent().tagName("tr").nextElementSibling().nextElementSibling().text();
                //parents().tagName("tr").first().nextElementSibling().nextElementSibling().text();
        if(null == candidate.getCandidateDetails())
            candidate.setCandidateDetails(new CandidateDetails());
        candidate.getCandidateDetails().setLocation(location.substring(0, location.indexOf("(")).trim());

        //set keyskill
        String keyskills = doc.getElementsMatchingOwnText("Keyskills").parents().tagName("tr").first().nextElementSibling().nextElementSibling().text();
        if (null != keyskills) {
            String[] allSkills = keyskills.split(",");
            List<CandidateSkillDetails> candidateSkillDetails = new ArrayList<>();
            Arrays.stream(allSkills).forEach(skill-> candidateSkillDetails.add(CandidateSkillDetails.builder().skill(skill).build()));
            candidate.setCandidateSkillDetails(candidateSkillDetails);
        }
    }
}
