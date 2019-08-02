/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.JobScreeningQuestions;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interface class for following services:
 * 1. Upload single candidate for a job
 * 2. Upload an excel file of candidates for a job
 *
 * @author : Shital Raval
 * Date : 16/7/19
 * Time : 4:44 PM
 * Class Name : IJobControllerMappingService
 * Project Name : server
 */
public interface IJobControllerMappingService {

    /**
     * Service method to add a individually added candidates to a job
     *
     * @param candidates the list of candidates to be added
     * @param jobId the job for which the candidate is to be added
     * @return the status of upload operation
     * @throws Exception
     */
    UploadResponseBean uploadIndividualCandidate(List<Candidate> candidates, Long jobId) throws Exception;

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
     * @return the status of upload operation
     * @throws Exception
     */
    UploadResponseBean uploadCandidateFromPlugin(Candidate candidate, Long jobId, MultipartFile candidateCv) throws Exception;


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
    void inviteCandidates(List<Long> jcmList) throws Exception;

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
    Candidate getCandidateProfile(Long jobCandidateMappingId) throws Exception;
}
