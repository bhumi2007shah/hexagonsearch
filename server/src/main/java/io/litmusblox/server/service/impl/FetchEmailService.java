/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

/**
 * @author : Shital Raval
 * Date : 28/11/19
 * Time : 9:58 AM
 * Class Name : FetchingEmail
 * Project Name : server
 */

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.CandidateDetails;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.User;
import io.litmusblox.server.service.IJobCandidateMappingService;
import io.litmusblox.server.service.IJobService;
import io.litmusblox.server.service.UploadResponseBean;
import io.litmusblox.server.uploadProcessor.impl.NaukriHtmlParser;
import io.litmusblox.server.uploadProcessor.impl.NaukriMassMailParser;
import io.litmusblox.server.utils.SentryUtil;
import io.litmusblox.server.utils.Util;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log4j2
public class FetchEmailService {

    @Autowired
    Environment environment;

    @Autowired
    IJobService jobService;

    @Autowired
    IJobCandidateMappingService jobCandidateMappingService;

    @Autowired
    NaukriHtmlParser naukriHtmlParser;

    @Autowired
    NaukriMassMailParser naukriMassMailParser;

    @Value("${spring.mail.host}")
    String mailServerHost;

    @Value("${spring.mail.username}")
    String userName;

    @Value("${spring.mail.password}")
    String password;

    @Value("${spring.mail.protocol}")
    String protocol;

    Pattern pattern = Pattern.compile(IConstant.REF_ID_MATCH_REGEX);

    public void processEmail() {
        try {
            //1) get the session object
            Properties properties = new Properties();
            properties.put("mail.store.protocol", protocol);

            Session emailSession = Session.getDefaultInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(userName, password);
                        }
                    });
            //2) create the POP3 store object and connect with the pop server
            Store emailStore = emailSession.getStore(protocol);
            emailStore.connect(mailServerHost,userName, password);

            //3) create the folder object and open it
            Folder emailFolder = emailStore.getFolder("Naukri");
            emailFolder.open(Folder.READ_WRITE);

            //4) retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            log.info("Number of unread messages: " + messages.length);

            for (Message message : messages) {
                try {
                    //check if mail is from an application from Naukri Massmail
                    if (null != message.getSubject() && ((Matcher)pattern.matcher(message.getSubject())).find()) {
                        //check if the mail is an application from Naukri
                        if (null != message.getSubject()) {
                            log.info("Subject: {}", message.getSubject());
                            MailData mailData = new MailData();
                            mailData.setJobFromReference(findJobForEmailSubject(message.getSubject()));

                            if (null == mailData.getJobFromReference()) {
                                log.error("Could not find job to process email with subject: {}", message.getSubject());
                                //mark the mail as read to skip processing it in the next round
                                message.setFlag(Flags.Flag.SEEN, true);
                            } else {

                                writePart(message, mailData, !(message.getSubject().contains(IConstant.NAUKRI_SUBJECT_STRING)));
                                message.setFlag(Flags.Flag.SEEN, true);
                                if (null != mailData.getFileName()) {
                                    if (null == mailData.getCandidateFromMail().getCandidateDetails())
                                        mailData.getCandidateFromMail().setCandidateDetails(new CandidateDetails());
                                    mailData.getCandidateFromMail().getCandidateDetails().setCvFileType("." + Util.getFileExtension(mailData.getFileName()));
                                }
                                UploadResponseBean response = jobCandidateMappingService.uploadCandidateFromPlugin(mailData.getCandidateFromMail(), mailData.getJobFromReference().getId(), null, Optional.of(mailData.getJobFromReference().getCreatedBy()));
                                if (IConstant.UPLOAD_STATUS.Success.name().equals(response.getStatus()) && null != mailData.getFileName())
                                    saveCandidateCv(mailData);
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Error processing mail with subject: {} \n Error message: {}", message.getSubject(), e.getMessage());

                    Map<String, String> breadCrumb = new HashMap<>();
                    breadCrumb.put("Mail Subject", message.getSubject());
                    breadCrumb.put("Error", e.getMessage());
                    SentryUtil.logWithStaticAPI(null, "Error processing Naukri application mail", breadCrumb);
                }
            }

            //5) close the store and folder objects
            emailFolder.close(false);
            emailStore.close();

        } catch (NoSuchProviderException e) {
            log.error("Error processing Naukri application mail {}" , e.getMessage());
            e.printStackTrace();
        } catch (MessagingException e) {
            log.error("Error processing Naukri application mail {}" , e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("Error processing Naukri application mail {}" , e.getMessage());
            e.printStackTrace();
        }
    }

    //write the candidate cv
    private void saveCandidateCv(MailData mailData) throws IOException {
        StringBuffer fileLocation = new StringBuffer(environment.getProperty(IConstant.REPO_LOCATION)).append(IConstant.CANDIDATE_CV).append(File.separator).append(mailData.getJobFromReference().getId());
        File file = new File(fileLocation.toString());
        if (!file.exists()) {
            file.mkdirs();
        }

        fileLocation.append(File.separator).append(mailData.getCandidateFromMail().getId()).append(".").append(Util.getFileExtension(mailData.getFileName()));

        Files.copy(mailData.getFileStream(), Paths.get(fileLocation.toString()), StandardCopyOption.REPLACE_EXISTING);
        new File(fileLocation.toString());
    }

    private Job findJobForEmailSubject(String subject) {
        Matcher matcher = pattern.matcher(subject);
        String jobReferenceId = null;
        if(matcher.find()){
            jobReferenceId = matcher.group();
            log.info("Extracted jobReferenceId: {}", jobReferenceId);
        }
        else{
            log.error("Reference Id not found in subject : {}", subject);
        }
        try {
            //UUID uuidFromString = UUID.fromString(jobReferenceId.substring(0,jobReferenceId.indexOf(',')).trim());
            assert jobReferenceId != null;
            // job reference id for testing perpose
            //jobReferenceId = "6aa11e4d-3e5f-4f6f-ae3b-61fbc49f8ab2";
            return jobService.findByJobReferenceId(UUID.fromString(jobReferenceId));
        } catch (Exception e) {
            log.error("Error while converting job reference to UUID.");
            return null;
        }
        //following was for test purpose only
        //return jobService.findByJobReferenceId(UUID.fromString("f3469d73-1662-11ea-92f0-74e5f9b964b9"));
    }


    public void writePart(Part p, MailData mailData, boolean naukriMassMail) throws Exception {

        log.info("CONTENT-TYPE: " + p.getContentType());

        //check if the content is plain text
        if (p.isMimeType("text/plain")) {
           // log.info("In plain text block\n{}",(String) p.getContent());
        }
        //check if the content has attachment
        else if (p.isMimeType("multipart/*")) {
            log.info("In Multipart block");
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
                writePart(mp.getBodyPart(i), mailData, naukriMassMail);
        }
        //check if the content is a nested message
        else if (p.isMimeType("message/rfc822")) {
            log.info("In Nested Message block");
            writePart((Part) p.getContent(), mailData, naukriMassMail);
        }
        else {
            log.info("In else block");
            Object o = p.getContent();
            if (o instanceof String) {
                //log.info("String Message:\n {}", (String)o);
                if(naukriMassMail){
                    mailData.setCandidateFromMail(naukriMassMailParser.parseData((String) o, mailData.getJobFromReference().getCreatedBy()));
                }else {
                    mailData.setCandidateFromMail(naukriHtmlParser.parseData((String) o, mailData.getJobFromReference().getCreatedBy()));
                }
            } else if (o instanceof InputStream) {
                log.info("Input stream: File");
                mailData.setFileName(p.getContentType().substring(p.getContentType().indexOf("name=") + 5).replaceAll("\"",""));
                mailData.setFileStream((InputStream) o);
            } else {
                log.info("Unknown type: ");
                //log.info(o.toString());
            }
        }

    }


    @Data
    class MailData {
        Candidate candidateFromMail;
        Job jobFromReference;
        User createdBy;
        String fileName;
        InputStream fileStream;
    }
}