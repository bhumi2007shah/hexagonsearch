/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.JobCandidateMapping;
import io.litmusblox.server.model.User;
import io.litmusblox.server.service.CandidateInteractionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Repository class for JobCandidateMapping
 *
 * @author : Shital Raval
 * Date : 10/7/19
 * Time : 5:23 PM
 * Class Name : JobCandidateMappingRepository
 * Project Name : server
 */
public interface JobCandidateMappingRepository extends JpaRepository<JobCandidateMapping, Long> {

    //Both the methods used not found so I have removed.

    //find count of candidates per stage
    @Transactional(readOnly = true)
    @Query(value = "select stage, count(candidate_id) from job_candidate_mapping where job_id=:jobId and rejected is false group by stage", nativeQuery = true)
    List<Object[]> findCandidateCountByStage(Long jobId) throws Exception;

    @Transactional(readOnly = true)
    @Query(value = "select stage, count(candidate_id) from job_candidate_mapping where id in (Select jcm_id from hiring_manager_workspace_details where user_id=:userId and job_id=:jobId) and rejected is false group by stage", nativeQuery = true)
    List<Object[]> findCandidateCountByStageForHiringManager(Long userId, Long jobId) throws Exception;

    @Transactional(readOnly = true)
    @Query(value = "select count(candidate_id) from job_candidate_mapping where job_id=:jobId and rejected is true", nativeQuery = true)
    int findRejectedCandidateCount(Long jobId) throws Exception;

    @Transactional(readOnly = true)
    @Query(value = "select count(candidate_id) from job_candidate_mapping where id in (Select jcm_id from hiring_manager_workspace_details where user_id=:userId and job_id=:jobId) and rejected is true", nativeQuery = true)
    int findRejectedCandidateCountForHiringManager(Long userId, Long jobId) throws Exception;

    //find count of candidates per stage
    @Transactional
    @Query(value = "select job_candidate_mapping.job_id, stage_step_master.stage, count(candidate_id) from job_candidate_mapping, stage_step_master\n" +
            "where job_candidate_mapping.job_id in :jobIds\n" +
            "and job_candidate_mapping.stage = stage_step_master.id\n" +
            "and job_candidate_mapping.rejected=:rejected\n" +
            "group by job_candidate_mapping.job_id, stage_step_master.stage order by job_candidate_mapping.job_id", nativeQuery = true)
    List<Object[]> findCandidateCountByStageJobIds(List<Long> jobIds, boolean rejected) throws Exception;

    //find by job and Candidate
    @Transactional
    JobCandidateMapping findByJobAndCandidate(Job job, Candidate candidate);

    //find by jobId and CandidateId
    @Transactional
    JobCandidateMapping findByJobIdAndCandidateId(Long jobId, Long candidateId);

    @Query(value = "select COUNT(jcm) from JOB_CANDIDATE_MAPPING jcm where jcm.CREATED_ON >=:createdOn and jcm.CREATED_BY =:user", nativeQuery = true)
    Integer getUploadedCandidateCount(@Param("createdOn") Date createdOn, @Param("user") User user);

    @Transactional
    JobCandidateMapping findByChatbotUuid(UUID uuid) throws Exception;

    @Transactional
    @Query(value = "select j.id as jobId, j.job_title as jobTitle, ssm.step as currentStatus, jcm.created_on as sourcedOn,(CASE when jcm.rejected = 't' then CONCAT('Rejected', ' - ', ssm.step) else ssm.step end) as lastStage, \n" +
            "(select array_to_string(array(select CONCAT(first_name, ' ', last_name) from users where id in (select (UNNEST(j.hiring_manager)))),', ')) as hiringManager, (select array_to_string(array(select CONCAT(first_name, ' ', last_name) from users where id in (select (UNNEST(j.recruiter)))),', ')) as recruiter \n" +
            "from job_candidate_mapping jcm inner join job j on j.id = jcm.job_id inner join stage_step_master ssm on ssm.id = jcm.stage where jcm.candidate_id =:candidateId and j.company_id =:companyId order by jcm.created_on desc;", nativeQuery = true)
    List<CandidateInteractionHistory> getCandidateInteractionHistoryByCandidateId(Long candidateId, Long companyId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update job_candidate_mapping set stage =:newStageId, rejected = false, updated_by =:updatedBy, updated_on =:updatedOn where stage =:oldStageId and id in :jcmId")
    void updateStageStepId(
            List<Long> jcmId,
            Long oldStageId,
            Long newStageId,
            Long updatedBy,
            Date updatedOn
    );

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update job_candidate_mapping set " +
            "screening_by = case when screening_by is null then :screeningBy else screening_by end, " +
            "screening_on = case when screening_by is null then :screeningOn else screening_on end, " +
            "stage =:newStageId, rejected = false, updated_by =:updatedBy, updated_on =:updatedOn " +
            "where stage =:oldStageId and id in :jcmList")
    void setScreenedByAndOn(List<Long> jcmList, String screeningBy, Date screeningOn, Long oldStageId, Long newStageId, Long updatedBy, Date updatedOn);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update job_candidate_mapping set " +
            "submitted_by = case when submitted_by is null then :submittedBy else submitted_by end, " +
            "submitted_on = case when submitted_on is null then :submittedOn else submitted_on end, " +
            "stage =:newStageId, rejected = false, updated_by =:updatedBy, updated_on =:updatedOn where stage =:oldStageId and id in :jcmList")
    void setSubmittedByAndOn(List<Long> jcmList, String submittedBy, Date submittedOn, Long oldStageId, Long newStageId, Long updatedBy, Date updatedOn);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update job_candidate_mapping set " +
            "make_offer_by = case when make_offer_by is null then :makeOfferBy else make_offer_by end, " +
            "make_offer_on = case when make_offer_on is null then :makeOfferOn else make_offer_on end, " +
            "stage =:newStageId, rejected = false, updated_by =:updatedBy, updated_on =:updatedOn where stage =:oldStageId and id in :jcmList")
    void setMakeOfferByAndOn(List<Long> jcmList, String makeOfferBy, Date makeOfferOn, Long oldStageId, Long newStageId, Long updatedBy, Date updatedOn);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update job_candidate_mapping set " +
            "offer_on = case when offer_on is null then :offerOn else offer_on end, " +
            "offer_by = case when offer_by is null then :offerBy else offer_by end, " +
            "stage =:newStageId, rejected = false, updated_by =:updatedBy, updated_on =:updatedOn where stage =:oldStageId and id in :jcmList")
    void setOfferByAndOn(List<Long> jcmList, String offerBy, Date offerOn, Long oldStageId, Long newStageId, Long updatedBy, Date updatedOn);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update job_candidate_mapping set " +
            "hired_by = case when hired_by is null then :hiredBy else hired_by end, " +
            "hired_on = case when hired_on is null then :hiredOn else hired_on end, " +
            "stage =:newStageId, rejected = false, updated_by =:updatedBy, updated_on =:updatedOn where stage =:oldStageId and id in :jcmList")
    void setHiredByAndOn(List<Long> jcmList, String hiredBy, Date hiredOn, Long oldStageId, Long newStageId, Long updatedBy, Date updatedOn);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update job_candidate_mapping set " +
            "rejected_by = case when rejected_by is null then :rejectedBy else rejected_by end, " +
            "rejected_on = case when rejected_on is null then :rejectedOn else rejected_on end, " +
            "rejected = true, candidate_rejection_value =:rejectionReason, updated_by =:updatedBy, updated_on =:updatedOn where id in :jcmList")
    void setRejectedByAndOn(List<Long> jcmList, String rejectedBy, Date rejectedOn, String rejectionReason, Long updatedBy, Date updatedOn);

    @Transactional(readOnly = true)
    @Query(nativeQuery = true, value = "select count(distinct stage) from job_candidate_mapping where id in :jcmList")
    int countDistinctStageForJcmList(List<Long> jcmList) throws Exception;

    @Modifying
    @Query(nativeQuery = true, value = "update job_candidate_mapping set rejected=true,candidate_rejection_value =:candidateRejectionValue, updated_by=:updatedBy, updated_on = :updatedOn where id in :jcmList")
    void updateForRejectStage(
            List<Long> jcmList,
            String candidateRejectionValue,
            Long updatedBy,
            Date updatedOn
    );

    @Transactional
    @Query(value = "select count(jcd.id)\n" +
            "from jcm_communication_details jcd, job_candidate_mapping jcm\n" +
            "where jcd.chat_invite_flag is true\n" +
            "and jcm.updated_by=:userId\n" +
            "and jcd.jcm_id = jcm.id", nativeQuery = true)
    Integer getInviteCount(Long userId);

    @Transactional
    @Query(value = "SELECT sum((chatbot_status LIKE 'Complete')\\:\\:INT) AS completeCount, sum((chatbot_status LIKE 'Incomplete')\\:\\:INT) AS incompleteCount\n" +
            "FROM job_candidate_mapping\n" +
            "where updated_by =:userId", nativeQuery = true)
    List<Object[]> getChatbotCountCompletedAndInCompleted(Long userId);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = "update job_candidate_mapping set chatbot_status=:chatbotStatus where id in :jcmList ")
    void updateJcmSetStatus(String chatbotStatus, List<Long>jcmList);

    @Transactional (readOnly = true)
    @Query(nativeQuery = true, value = "SELECT  sum((chatbot_status LIKE 'Invited')\\:\\:INT) AS invitedCount,\n" +
            "sum((chatbot_status LIKE 'Not Interested')\\:\\:INT) AS notInterestedCount,\n" +
            "sum((chatbot_status LIKE 'Complete')\\:\\:INT) AS completeCount,\n" +
            "sum((chatbot_status LIKE 'Incomplete')\\:\\:INT) AS incompleteCount\n" +
            "FROM job_candidate_mapping, stage_step_master\n" +
            "where job_candidate_mapping.job_id = :jobId\n" +
            "and job_candidate_mapping.rejected = false\n" +
            "and job_candidate_mapping.stage = stage_step_master.id\n" +
            "and stage_step_master.stage = :stage")
    List<Object[]> getCandidateCountPerStage(Long jobId, String stage) throws Exception;

    @Transactional
    @Query(nativeQuery = true, value = "select * from job_candidate_mapping where chatbot_status is null and job_id in (select id from job where auto_invite = 't') and stage=(select id from stage_step_master where stage='Sourcing') and (job_candidate_mapping.mobile is not null or job_candidate_mapping.email not like '%notavailable.io')")
    List<JobCandidateMapping> getNewAutoSourcedJcmList();

    List<JobCandidateMapping> findAllByJobId(Long jobId);

    Long countByJobId(Long jobId);

    @Transactional(readOnly = true)
    @Query(nativeQuery = true, value = "select count(*) as count from job_candidate_mapping where job_id=:jobId and created_on>=:startDate and created_on<=:endDate")
    Long countByJobIdAndDate(Long jobId, Date startDate, Date endDate);

    @Transactional(readOnly = true)
    @Query(nativeQuery = true, value = "select count(*) as count from job_candidate_mapping where job_id=:jobId and chatbot_status=:status")
    Long countByJobIdAndStatus(Long jobId, String status);

    @Transactional(readOnly = true)
    @Query(nativeQuery = true, value = "select distinct(job_id) from job_candidate_mapping where id in :jcmList")
    List<Long> findDistinctJobIdByJcmID(List<Long> jcmList);

    @Transactional(readOnly = true)
    JobCandidateMapping findByEmailAndJobId(String email,Long jobId);

    @Transactional
    @Query(value = "select * from job_candidate_mapping where IS_CREATED_ON_SEARCHENGINE='f' order by ID asc limit 50", nativeQuery = true)
        List<JobCandidateMapping> findJcmNotInSearchEngine();

    @Transactional
    @Query(value = "select * from job_candidate_mapping where id = (select id from (select id,unnest(array[created_on, updated_on]) from job_candidate_mapping where candidate_id =:candidateId and job_id in (select id from job where company_id =:companyId)) as jcm_dates where jcm_dates.unnest < current_date + interval '1' day  order by jcm_dates.unnest desc limit 1)", nativeQuery = true)
    JobCandidateMapping getLastUpdatedJCMForCandidate(Long candidateId, Long companyId);

    @Transactional
    @Query(value = "select jcm.job_id, ssm.stage, count(jcm.candidate_id) \n" +
            "from job_candidate_mapping jcm\n" +
            "inner join job j on j.id = jcm.job_id\n" +
            "inner join stage_step_master ssm on ssm.id = jcm.stage\n" +
            "inner join hiring_manager_workspace hmw on hmw.jcm_id = jcm.id\n" +
            "inner join jcm_profile_sharing_details jpsd on jpsd.id = hmw.share_profile_id\n" +
            "where jcm.job_id in :jobIds and jcm.stage = ssm.id and j.status in ('Draft', 'Live') and jpsd.receiver_id =:hiringManagerId and jcm.rejected =:rejected group by jcm.job_id, ssm.stage order by jcm.job_id;", nativeQuery = true)
    List<Object[]> findCandidateCountByStageJobIdsForHmw(List<Long> jobIds, boolean rejected, Long hiringManagerId) throws Exception;

    @Transactional
    List<JobCandidateMapping> findByCandidateId(Long candidateId);

    @Query(value = "select * from job_candidate_mapping where candidate_id=:candidateId order by id desc limit 1", nativeQuery = true)
    JobCandidateMapping getUpdatedJcm(Long candidateId);

    @Query(value = "select * from job_candidate_mapping where job_id =:companyId and candidate_id in (select candidate_id from candidate_email_history where email =:email)", nativeQuery = true)
    JobCandidateMapping findByEmailAndCompanyID(String email,Long companyId);

    @Query(value = "select * from job_candidate_mapping where job_id =:companyId and candidate_id in (select candidate_id from candidate_mobile_history where mobile =:mobile)", nativeQuery = true)
    JobCandidateMapping findByMobileAndCompanyID(String mobile,Long companyId);

}