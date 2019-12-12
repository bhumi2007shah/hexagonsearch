package io.litmusblox.server.repository;

import io.litmusblox.server.model.JcmHistory;
import io.litmusblox.server.model.JobCandidateMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
}
