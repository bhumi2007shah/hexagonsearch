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
import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.User;
import io.litmusblox.server.service.IJobCandidateMappingService;
import io.litmusblox.server.service.IJobService;
import io.litmusblox.server.service.UploadResponseBean;
import io.litmusblox.server.uploadProcessor.impl.NaukriHtmlParser;
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
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

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

    private static String naukriSubjectString = "Naukri.com -";

    @Value("${spring.mail.host}")
    String mailServerHost;

    @Value("${spring.mail.username}")
    String userName;

    @Value("${spring.mail.password}")
    String password;

    @Value("${spring.mail.protocol}")
    String protocol;

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
            Folder emailFolder = emailStore.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);

            //4) retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            log.info("Number of unread messages: " + messages.length);

            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                log.info("Subject: {}", message.getSubject());
                //check if the mail is an application from Naukri
                if (null != message.getSubject() && message.getSubject().indexOf(naukriSubjectString) != -1) {
                    MailData mailData = new MailData();
                    mailData.setJobFromReference(findJobForEmailSubject(message.getSubject()));

                    writePart(message, mailData);
                    message.setFlag(Flags.Flag.SEEN, true);
                    UploadResponseBean response = jobCandidateMappingService.uploadCandidateFromPlugin(mailData.getCandidateFromMail(), mailData.getJobFromReference().getId(), null, Optional.of(mailData.getJobFromReference().getCreatedBy()));
                    if (IConstant.UPLOAD_STATUS.Success.name().equals(response.getStatus()))
                        saveCandidateCv(mailData);
                    break;
                }
            }

            //5) close the store and folder objects
            emailFolder.close(false);
            emailStore.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
        Files.write(Paths.get(fileLocation.toString()), mailData.getFileData().getBytes(), StandardOpenOption.CREATE);
    }

    private Job findJobForEmailSubject(String subject) {
        String jobReferenceId = subject.substring(subject.indexOf(naukriSubjectString) + naukriSubjectString.length());
        log.info("Extracted jobReferenceId: {}", jobReferenceId.substring(0,jobReferenceId.indexOf(',')));
      //TODO: Uncomment the following
       // return jobService.findByJobReferenceId(UUID.fromString(jobReferenceId.substring(0,jobReferenceId.indexOf(','))));
        return jobService.findByJobReferenceId(UUID.fromString("f3469d73-1662-11ea-92f0-74e5f9b964b9"));
    }


    public void writePart(Part p, MailData mailData) throws Exception {

        log.info("CONTENT-TYPE: " + p.getContentType());

        //check if the content is plain text
        if (p.isMimeType("text/plain")) {
            log.info("In plain text block\n{}",(String) p.getContent());
        }
        //check if the content has attachment
        else if (p.isMimeType("multipart/*")) {
            log.info("In Multipart block");
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
                writePart(mp.getBodyPart(i), mailData);
        }
        //check if the content is a nested message
        else if (p.isMimeType("message/rfc822")) {
            log.info("In Nested Message block");
            writePart((Part) p.getContent(), mailData);
        }
        else {
            log.info("In else block");
            Object o = p.getContent();
            if (o instanceof String) {
                log.info("String Message:\n {}", (String)o);
                mailData.setCandidateFromMail(naukriHtmlParser.parseData((String)o, mailData.getJobFromReference().getCreatedBy()));
            } else if (o instanceof InputStream) {
                log.info("Input stream: File");
                InputStream is = (InputStream) o;
                int c;

                StringBuffer fileContents = new StringBuffer();
                while ((c = is.read()) != -1)
                    fileContents.append(c);

                is.close();

                mailData.setFileData(fileContents.toString());
                mailData.setFileName(p.getContentType().substring(p.getContentType().indexOf("name=") + 5).replaceAll("\"",""));
            } else {
                log.info("Unknown type: ");
                log.info(o.toString());
            }
        }

    }


    @Data
    class MailData {
        Candidate candidateFromMail;
        Job jobFromReference;
        User createdBy;
        String fileData;
        String fileName;
    }

    /*
    public static void main(String[] args) {

        String host = "smtp.gmail.com";//change accordingly
        String mailStoreType = "pop3";
        final String username= "shital@hexagonsearch.com";
        final String password= "Hexagon01";//change accordingly

        new FetchEmailService().receiveEmail(host, mailStoreType, username, password);
    }*/
}