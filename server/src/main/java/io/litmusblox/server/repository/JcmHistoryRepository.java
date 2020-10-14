package io.litmusblox.server.repository;

import io.litmusblox.server.model.JcmHistory;
import io.litmusblox.server.model.JobCandidateMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : sameer
 * Date : 12/09/19
 * Time : 5:54 PM
 * Class Name : JcmHistoryRepository
 * Project Name : server
 */
@Repository
public interface JcmHistoryRepository extends JpaRepository<JcmHistory, Long> {
    @Transactional
    void deleteByJcmId(JobCandidateMapping jobCandidateMapping);

    @Transactional(readOnly = true)
    @Query(value = "select * from jcm_history where jcm_id in \n" +
            "(select id from job_candidate_mapping where job_id in \n" +
            "(select id from job where company_id =:companyId) and candidate_id =:candidateId) order by updated_on desc", nativeQuery = true)
    List<JcmHistory> getJcmHistoryList(Long companyId, Long candidateId);

    @Transactional(readOnly = true)
    List<JcmHistory> findByJcmIdAndCallLogOutComeIgnoreCase(JobCandidateMapping jcmId, String callLogOutcome);
}
