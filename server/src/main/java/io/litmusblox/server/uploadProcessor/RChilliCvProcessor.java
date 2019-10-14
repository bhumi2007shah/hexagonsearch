/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.service.IJobCandidateMappingService;
import io.litmusblox.server.service.MasterDataBean;
import io.litmusblox.server.utils.RestClient;
import io.litmusblox.server.utils.SentryUtil;
import io.litmusblox.server.utils.StoreFileUtil;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class to process the CV uploaded against RChilli application
 *
 * @author : Shital Raval
 * Date : 21/8/19
 * Time : 1:06 PM
 * Class Name : DragAndDropCvProcessor
 * Project Name : server
 */
@Log4j2
@Service
public class RChilliCvProcessor {

    @Autowired
    Environment environment;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    IUploadDataProcessService uploadDataProcessService;

    @Autowired
    JobCandidateMappingRepository jobCandidateMappingRepository;

    @Autowired
    IJobCandidateMappingService jobCandidateMappingService;

    @Autowired
    CvParsingDetailsRepository cvParsingDetailsRepository;

    @Resource
    CandidateRepository candidateRepository;

    @Transactional(readOnly = true)
    User getUser(long userId) {
        return userRepository.findById(userId).get();
    }

    @Transactional(readOnly = true)
    Job getJob(long jobId) {
        return jobRepository.findById(jobId).get();
    }

    /**
     * Service method to process the CV uploaded against RChilli application
     *
     * @param filePath
     */
    public void processFile(String filePath) {
        log.info("Inside processFile method");
        // remove task steps
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        String[] s = fileName.split("_");

        User user = getUser(Long.parseLong(s[0]));
        Job job = getJob(Long.parseLong(s[1]));

        Candidate candidate = null;
        String rchilliFormattedJson = null, rchilliJsonResponse = null;
        ResumeParserDataRchilliBean bean = null;
        long rchilliResponseTime = 0L, candidateId=0L;
        boolean isCandidateFailedToProcess = false, rChilliErrorResponse = false;

        RestClient rest = RestClient.getInstance();
        String jsonString = "{\"url\":\"" + environment.getProperty(IConstant.FILE_STORAGE_URL) + fileName + "\",\"userkey\":\"" + environment.getProperty(IConstant.USER_KEY) + "\",\"version\":\"" + environment.getProperty(IConstant.VERSION)
                + "\",\"subuserid\":\"" + environment.getProperty(IConstant.SUB_USER_ID) + "\"}";
        try {
            long startTime = System.currentTimeMillis();
            rchilliJsonResponse=rest.consumeRestApi(jsonString, environment.getProperty(IConstant.RCHILLI_API_URL), HttpMethod.POST,null);
            rchilliResponseTime = System.currentTimeMillis() - startTime;
            log.info("Recevied response from RChilli in " + rchilliResponseTime + "ms.");
            if(null != rchilliJsonResponse && rchilliJsonResponse.contains("errorcode") && rchilliJsonResponse.contains("errormsg")) {
                rChilliErrorResponse = true;
                isCandidateFailedToProcess = true;
            }

             if(!rChilliErrorResponse) {

                 rchilliJsonResponse=rchilliJsonResponse.substring(rchilliJsonResponse.indexOf(":")+1,rchilliJsonResponse.lastIndexOf("}"));
                 rchilliFormattedJson=rchilliJsonResponse.substring(0, rchilliJsonResponse.indexOf("DetailResume")-7)+"\n"+"}";

                //log.info("RchilliJsonResponse  : "+rchilliJsonResponse);
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                bean = mapper.readValue(rchilliJsonResponse, ResumeParserDataRchilliBean.class);
                //log.info("ResumeParserDataRchilliBean :"+resumeParserDataRchilliBean);
                 String cvType="."+Util.getFileExtension(fileName);
                candidate = setCandidateModel(bean, user, cvType);

                candidateId = processCandidate(candidate, user, job);
                if(candidateId==0)
                    isCandidateFailedToProcess=true;
                else{
                    isCandidateFailedToProcess=false;
                    candidate.setId(candidateId);
                }
            }
            else {
                log.error("Failed to process CV against RChilli: " + rchilliJsonResponse);
                 Map<String, String> breadCrumb = new HashMap<>();
                 breadCrumb.put("User Id",user.getId().toString());
                 breadCrumb.put("User email",user.getEmail());
                 breadCrumb.put("Job id",job.getId().toString());
                 breadCrumb.put("Drag & Drop filename", fileName);
                 SentryUtil.logWithStaticAPI(null,"Failed to process CV against RChilli",breadCrumb);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error while processing candidate in drag and drop : " + ((null != candidate) ? candidate.getEmail() : user.getEmail()) + " : " + e.getMessage(), HttpStatus.BAD_REQUEST);
            isCandidateFailedToProcess = true;
        }

        try {
            //For converting multipart file create private method
            File file = new File(filePath);
            MultipartFile multipartFile = createMultipartFile(file);
            if(isCandidateFailedToProcess && rChilliErrorResponse)
                StoreFileUtil.storeFile(multipartFile, job.getId(), environment.getProperty(IConstant.REPO_LOCATION), IConstant.ERROR_FILES,null, user);
            else{
                if (isCandidateFailedToProcess && candidateId==0)
                    StoreFileUtil.storeFile(multipartFile, job.getId(), environment.getProperty(IConstant.REPO_LOCATION), IConstant.ERROR_FILES, candidate, null);
                else
                    StoreFileUtil.storeFile(multipartFile, job.getId(), environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(), candidate, null);

            }
            file.delete();
        } catch (Exception ex) {
            log.error("Error while save candidate resume in drag and drop : " + fileName + " : " + ex.getMessage(), HttpStatus.BAD_REQUEST);
            log.error("For CandidateId : "+candidateId+", Email : "+candidate.getEmail()+", Mobile : "+candidate.getMobile());
        }
        addCvParsingDetails(fileName, rchilliResponseTime, (null!=rchilliFormattedJson)?rchilliFormattedJson:rchilliJsonResponse, isCandidateFailedToProcess, bean, (candidate != null)?candidate.getUploadErrorMessage():null, candidateId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private long processCandidate(Candidate candidate, User user, Job job) {

        int candidateProcessed = jobCandidateMappingRepository.getUploadedCandidateCount(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()), user);

        if (candidateProcessed >= MasterDataBean.getInstance().getConfigSettings().getCandidatesPerFileLimit()) {
            log.error(IErrorMessages.MAX_CANDIDATE_PER_FILE_EXCEEDED + " : user id : " + user.getId());
        }
        //check for daily limit per user
        if (candidateProcessed >= MasterDataBean.getInstance().getConfigSettings().getDailyCandidateUploadPerUserLimit()) {
            log.error(IErrorMessages.MAX_CANDIDATES_PER_USER_PER_DAY_EXCEEDED + " : user id : " + user.getId());
        }
        try {
            candidate = uploadDataProcessService.validateDataAndSaveJcmAndJcmCommModel(null, candidate, user, !candidate.getMobile().isEmpty(), job);
        } catch (ValidationException ve) {
            candidate.setUploadErrorMessage(ve.getErrorMessage());
            log.error("Error while validate candidate data and save jcm received from RChilliJson : " + ve.getErrorMessage()+", Email : "+candidate.getEmail()+", Mobile : "+candidate.getMobile());
            //return 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error while validate candidate data and save jcm received from RChilliJson : " + e.getMessage()+", CandidateEmail : "+candidate.getEmail()+", CandidateMobile : "+candidate.getMobile());
            //return 0;
        }

        try {
            jobCandidateMappingService.saveCandidateSupportiveInfo(candidate, user);
        }catch (ValidationException ve){
            ve.printStackTrace();
            log.error("Error while saving candidate supportive information received from RChilliJson : " + ve.getMessage()+", CandidateEmail : "+candidate.getEmail()+", CandidateMobile : "+candidate.getMobile());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error while saving candidate supportive information received from RChilliJson : " + e.getMessage()+", CandidateEmail : "+candidate.getEmail()+", CandidateMobile : "+candidate.getMobile());
        }
        return candidate.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void addCvParsingDetails(String fileName, long rchilliResponseTime, String rchilliFormattedJson, Boolean isCandidateFailedToProcess, ResumeParserDataRchilliBean bean, String errorMessage, Long candidateId) {
        log.info("Inside addCvParsingDetails method");
        try {
            //Add cv_parsing_details
            CvParsingDetails cvParsingDetails = new CvParsingDetails();
            cvParsingDetails.setCvFileName(fileName);
            cvParsingDetails.setProcessedOn(new Date());
            cvParsingDetails.setProcessingTime(rchilliResponseTime);

            if(null != candidateId || candidateId != 0)
                cvParsingDetails.setCandidateId(candidateId);

            if (isCandidateFailedToProcess){
                cvParsingDetails.setProcessingStatus(IConstant.UPLOAD_STATUS.Failure.toString());
                cvParsingDetails.setRchilliJsonProcessed(false);
                log.info("CvParsingDetails status is Failure errorMessage : "+errorMessage);
            }else
                cvParsingDetails.setProcessingStatus(IConstant.UPLOAD_STATUS.Success.toString());

            if (null != bean) {
                cvParsingDetails.setParsingResponseHtml(bean.getHtmlResume());
                cvParsingDetails.setParsingResponseText(bean.getDetailResume());
            }
            cvParsingDetails.setParsingResponseJson(rchilliFormattedJson);
            cvParsingDetails.setErrorMessage(errorMessage);
            cvParsingDetailsRepository.save(cvParsingDetails);
            log.info("Save CvParsingDetails");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Candidate setCandidateModel(ResumeParserDataRchilliBean bean, User user, String cvType) {
        log.info("Inside setCandidateModel by rchilli data");
        String alternateMobile=null;
        String[] mobileString=null;
        String mobile=bean.getFormattedMobile().isEmpty() ? bean.getFormattedPhone() : bean.getFormattedMobile();

        if(mobile.contains(",")){
            mobileString =mobile.split(",");
            mobile = mobileString[0];
            alternateMobile=mobileString[1];
        }

        //Format mobile no
        mobile=Util.indianMobileConvertor(mobile);
        if(null!=alternateMobile)
            alternateMobile=Util.indianMobileConvertor(alternateMobile);

        Candidate candidate = new Candidate(bean.getFirstName(), bean.getLastName(), bean.getEmail(), mobile, null, new Date(), null);
        if(null!=alternateMobile)
            candidate.setAlternateMobile(alternateMobile);
        candidate.setCandidateName(bean.getFullName());
        candidate.setCandidateSource(IConstant.CandidateSource.DragDropCv.toString());
        if(user != null)
            candidate.setCountryCode(user.getCountryId().getCountryCode());

        CandidateDetails candidateDetails = new CandidateDetails();
        candidateDetails.setDateOfBirth(Util.convertStringToDate(bean.getDateOfBirth()));
        candidateDetails.setGender(bean.getGender());

        if(bean.getSkills().length()>255)
            candidateDetails.setKeySkills(bean.getSkills().substring(0,255));
        else
            candidateDetails.setKeySkills(bean.getSkills());

        candidateDetails.setMaritalStatus(bean.getMaritalStatus());
        candidateDetails.setCvFileType(cvType);

        if(bean.getFormattedAddress().isEmpty())
            candidateDetails.setCurrentAddress(bean.getAddress());
        else
            candidateDetails.setCurrentAddress(bean.getFormattedAddress());

        candidate.setCandidateDetails(candidateDetails);

        bean.getSegregatedQualification().getEducationSplit().forEach(educationSplit -> {
            CandidateEducationDetails candidateEducationDetails = new CandidateEducationDetails();

            if (educationSplit.getInstitution().getName().length() > 75)
                candidateEducationDetails.setInstituteName(educationSplit.getInstitution().getName().substring(0, 75));
            else
                candidateEducationDetails.setInstituteName(educationSplit.getInstitution().getName());

            candidateEducationDetails.setDegree(educationSplit.getDegree());
            if (!educationSplit.getEndDate().isEmpty()) {
                candidateEducationDetails.setYearOfPassing(Util.getYearFromStringDate(educationSplit.getEndDate()));
            }
            candidate.getCandidateEducationDetails().add(candidateEducationDetails);
        });

        bean.getSegregatedExperience().getWorkHistory().forEach(workHistory -> {
            CandidateCompanyDetails candidateCompanyDetails = new CandidateCompanyDetails();
            candidateCompanyDetails.setCompanyName(workHistory.getEmployer());
            candidateCompanyDetails.setDesignation(workHistory.getJobProfile().getTitle());
            candidateCompanyDetails.setLocation(workHistory.getJobLocation().getEmployerCity());
            candidateCompanyDetails.setStartDate(Util.convertStringToDate(workHistory.getStartDate()));
            candidateCompanyDetails.setEndDate(Util.convertStringToDate(workHistory.getEndDate()));
            candidate.getCandidateCompanyDetails().add(candidateCompanyDetails);

            workHistory.getProjects().forEach(projects -> {
                CandidateProjectDetails candidateProjectDetails = new CandidateProjectDetails();
                candidateProjectDetails.setCompanyName(workHistory.getEmployer());
                candidateProjectDetails.setSkillsUsed(projects.getUsedSkills());
                candidate.getCandidateProjectDetails().add(candidateProjectDetails);
            });
        });

        bean.getWebSites().getWebsite().forEach(webSite -> {
            CandidateOnlineProfile candidateOnlineProfile = new CandidateOnlineProfile();
            candidateOnlineProfile.setUrl(webSite.getUrl());
            candidateOnlineProfile.setProfileType(webSite.getType());
            candidate.getCandidateOnlineProfiles().add(candidateOnlineProfile);
        });

        if(!bean.getLanguageKnown().isEmpty()){
            for (String language : bean.getLanguageKnown().split(",")) {
                CandidateLanguageProficiency candidateLanguageProficiency=new CandidateLanguageProficiency();
                candidateLanguageProficiency.setLanguage(language);
                candidate.getCandidateLanguageProficiencies().add(candidateLanguageProficiency);
            }
        }

        bean.getSkillKeywords().getSkillSet().forEach(skillSet -> {
            CandidateSkillDetails candidateSkillDetails=new CandidateSkillDetails();
            candidateSkillDetails.setSkill(skillSet.getSkill());
            candidateSkillDetails.setExpInMonths(Long.parseLong(skillSet.getExperienceInMonths()));
            candidateSkillDetails.setLastUsed(Util.convertStringToDate(skillSet.getLastUsed()));
            candidate.getCandidateSkillDetails().add(candidateSkillDetails);
        });
        return candidate;
    }
    //Remove storeFile method because repeated code

   public void processRchilliJson(){
        log.info("inside processRchilliJson method");
        List<CvParsingDetails> cvParsingDetailsList = cvParsingDetailsRepository.findByRchilliJsonProcessed(false);
        for (CvParsingDetails cvParsingDetails : cvParsingDetailsList){
            updateCandidateInfo(cvParsingDetails);
        }
    }

    private void updateCandidateInfo(CvParsingDetails cvParsingDetails){
        log.info("inside updateCandidateInfo method");
        log.info("ProcessRchilliJson start for candidate : "+cvParsingDetails.getCandidateId());
        ResumeParserDataRchilliBean bean = null;
        StringBuffer errorFilePath=new StringBuffer();
        Long candidateId = null;
        Candidate candidateByData = null;
        Candidate candidate = null;
        if(null != cvParsingDetails.getCandidateId()){
             candidate = candidateRepository.findById(cvParsingDetails.getCandidateId()).orElse(null);
            if(null == candidate){
                log.error("Candidate not found For id "+cvParsingDetails.getCandidateId());
                return;
            }
        }else{
            log.error("Candidate id is null");
            return;
        }

        String[] s = cvParsingDetails.getCvFileName().split("_");
        User user = getUser(Long.parseLong(s[0]));
        Job job = getJob(Long.parseLong(s[1]));

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            bean = mapper.readValue(cvParsingDetails.getParsingResponseJson(), ResumeParserDataRchilliBean.class);
        }catch (Exception e){
            e.printStackTrace();
            log.error("Error while processing rchilli Json : " + candidate.getEmail()+ " : " + e.getMessage(), HttpStatus.BAD_REQUEST);
           // isCandidateFailedToProcess = true;
        }

        errorFilePath.append(environment.getProperty(IConstant.REPO_LOCATION)).append(IConstant.ERROR_FILES).append(File.separator).append(cvParsingDetails.getCvFileName());
        log.info("Get Error file from : "+errorFilePath);
        String cvFileType = "."+Util.getFileExtension(errorFilePath.toString());
        candidateByData = setCandidateModel(bean, null, cvFileType);
        candidateId = processCandidate(candidateByData, user, job);
        try {
            File file = new File(errorFilePath.toString());
            MultipartFile multipartFile = createMultipartFile(file);
            StoreFileUtil.storeFile(multipartFile, job.getId(), environment.getProperty(IConstant.REPO_LOCATION), IConstant.UPLOAD_TYPE.CandidateCv.toString(), candidate, null);

            cvParsingDetails.setRchilliJsonProcessed(true);
            cvParsingDetails.setProcessingStatus(IConstant.UPLOAD_STATUS.Success.toString());
            cvParsingDetailsRepository.save(cvParsingDetails);
            file.delete();
        } catch (Exception ex) {
            log.error("Error while get and save candidate resume : " + cvParsingDetails.getCvFileName() + " : " + ex.getMessage(), HttpStatus.BAD_REQUEST);
            log.error("For CandidateId : "+candidateId+", Email : "+candidate.getEmail()+", Mobile : "+candidate.getMobile());
        }
    }

    private MultipartFile createMultipartFile(File file) throws IOException {
        log.info("inside createMultipartFile method");
        //File file = new File(filePath);
        DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(), file.getParentFile());
        InputStream input = new FileInputStream(file);
        OutputStream os = fileItem.getOutputStream();
        int ret = input.read();
        while (ret != -1) {
            os.write(ret);
            ret = input.read();
        }
        os.flush();
        return new CommonsMultipartFile(fileItem);
    }
}
