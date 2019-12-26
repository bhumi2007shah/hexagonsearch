/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Interface class for following services:
 * 1. Upload single candidate for a job
 * 2. Upload an excel file of candidates for a job
 *
 * @author : Shital Raval
 * Date : 16/7/19
 * Time : 4:44 PM
 * Class Name : IJobCandidateMappingService
 * Project Name : server
 */
public interface IJobCandidateMappingService {

    /**
     * Service method to add a individually added candidates to a job
     *
     * @param candidates the list of candidates to be added
     * @param jobId the job for which the candidate is to be added
     * @param createdBy optional paramter, createdBy, will be supplied only when calling processing candidates from mail
     * @return the status of upload operation
     * @throws Exception
     */
    UploadResponseBean uploadIndividualCandidate(List<Candidate> candidates, Long jobId, boolean ignoreMobile, Optional<User> createdBy) throws Exception;

    /**
     * Service method to add candidates from a file in one of the supported formats
     *
     * @param multipartFile the file with candidate information
     * @param jobId the job for which the candidates have to be added
     * @param fileFormat the format of file, for e.g. Naukri, LB format
     * @return the status of upload operation
     * @throws Exception
     */
    UploadResponseBean uploadCandidatesFromFile(MultipartFile multipartFile, Long jobId, String fileFormat) throws Exception;

    /**
     * Service method to source and add a candidate from a plugin, for example Naukri plugin
     *
     * @param candidate the candidate to be added
     * @param jobId the job for which the candidate is to be added
     * @param candidateCv candidate Cv
     * @param createdBy optional paramter, createdBy, will be supplied only when calling processing candidates from mail
     * @return the status of upload operation
     * @throws Exception
     */
    UploadResponseBean uploadCandidateFromPlugin(Candidate candidate, Long jobId, MultipartFile candidateCv, Optional<User> createdBy) throws Exception;


    /**
     * Service method to capture candidate consent from chatbot
     * @param uuid the uuid corresponding to a unique jcm record
     * @param interest boolean to capture candidate consent
     * @throws Exception
     */
    void captureCandidateInterest(UUID uuid, boolean interest) throws Exception;

    /**
     * Service method to capture candidate response to screening questions from chatbot
     * @param uuid the uuid corresponding to a unique jcm record
     * @param candidateResponse the response provided by a candidate against each screening question
     * @throws Exception
     */
    void saveScreeningQuestionResponses(UUID uuid, Map<Long, List<String>> candidateResponse) throws Exception;

    /**
     * Service method to get all screening questions for the job
     * @param uuid the uuid corresponding to a unique jcm record
     * @return the list of job screening questions
     * @throws Exception
     */
    List<JobScreeningQuestions> getJobScreeningQuestions(UUID uuid) throws Exception;

    /**
     * Service method to invite candidates to fill chatbot for a job
     *
     * @param jcmList list of jcm ids for chatbot invitation
     * @throws Exception
     */
    InviteCandidateResponseBean inviteCandidates(List<Long> jcmList) throws Exception;

    /**
     * Service method to process sharing of candidate profiles with Hiring managers
     *
     * @param requestBean The request bean with information about the profile to be shared, the recepient name and recepient email address
     * @throws Exception
     */
    void shareCandidateProfiles(ShareCandidateProfileRequestBean requestBean);

    /**
     * Service method to capture hiring manager interest
     *
     * @param sharingId the uuid corresponding to which the interest needs to be captured
     * @param interestValue interested true / false response
     * @throws Exception
     */
    void updateHiringManagerInterest(UUID sharingId, Boolean interestValue);

    /**
     * Service method to fetch details of a single candidate for a job
     *
     * @param jobCandidateMappingId
     * @return candidate object with required details
     * @throws Exception
     */
    JobCandidateMapping getCandidateProfile(Long jobCandidateMappingId, Date hiringManagerInterestDate) throws Exception;

    /**
     * Service method to fetch details of a single candidate for a job
     *
     * @param profileSharingUuid uuid corresponding to the profile shared with hiring manager
     * @return candidate object with required details
     * @throws Exception
     */
    JobCandidateMapping getCandidateProfile(UUID profileSharingUuid) throws Exception;

    /**
     * Method to retrieve the job candidate mapping record based on the uuid
     * @param uuid the uuid against which the record is to be retrieved
     * @return the job candidate mapping
     * @throws Exception
     */
    JobCandidateMapping getJobCandidateMapping(UUID uuid) throws Exception;

    /**
     * Service method to upload candidates by means of drag and drop cv
     *
     * @param multipartFiles files to be processed to upload candidates
     * @param jobId the job for which the candidate is to be added
     * @return response bean with details about success / failure of each candidate file
     * @throws Exception
     */
    CvUploadResponseBean processDragAndDropCv(MultipartFile[] multipartFiles, Long jobId);

    /**
     * Method for save candidates supportive information like Company, project, language, skills etc
     *
     * @param candidate for which candidate add this info
     * @param loggedInUser user which is login currently
     */
    void saveCandidateSupportiveInfo(Candidate candidate, User loggedInUser) throws Exception;

    /**
     * Service to update tech response status received from scoring engine.
     *
     * @param requestBean bean with update information from scoring engine
     * @throws Exception
     */
    void updateTechResponseStatus(TechChatbotRequestBean requestBean) throws Exception;

    /**
     * Service to edit candidate info like:mobile,email,TotalExperience
     *
     * @param jobCandidateMapping updated data from JobCandidateMapping model
     */
    void editCandidate(JobCandidateMapping jobCandidateMapping);

    /**
     * Service to set a specific stage like Interview, Offer etc
     *
     * @param jcmList The list of candidates for the job that need to be moved to the specified stage
     * @param stage the new stage
     * @throws Exception
     */
    void setStageForCandidates(List<Long> jcmList, String stage) throws Exception;

    /**
     * Service to return error list for drag and drop CV's for a job
     *
     * @param jobId job id for which files with error wil be returned
     * @return List of RChilliErrorResponseBean which have file name, processed date, status, jcmId, candidate name if available
     * @throws Exception
     */
    List<ResponseBean> getRchilliError(Long jobId) throws Exception;

    /**
     * Service to retrieve candidate history based on jcmId
     *
     * @param jcmId
     * @return list of jcm
     */
    List<JcmHistory> retrieveCandidateHistory(Long jcmId);

    /**
     * Service to create jcm history for every comment by recruiter
     *
     * @param comment comment add by  recruiter
     * @param jcmId for which jcm we need to create jcm history
     * @param callOutCome callOutCome if callOutCome is present then set in jcm history
     */
    void addComment(String comment, Long jcmId, String callOutCome);

    /**
     * Service to upload resume against jcm
     * @param jcmId
     * @param candidateCv
     */
    void uploadResume(MultipartFile candidateCv, Long jcmId) throws Exception;
}
