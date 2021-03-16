/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;
import io.litmusblox.server.model.JcmOfferDetails;
import io.litmusblox.server.model.JobCandidateMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface JcmOfferDetailsRepository extends JpaRepository<JcmOfferDetails,Long> {
    @Transactional(readOnly = true)
    JcmOfferDetails findByJcmId(JobCandidateMapping jcmId);

    @Query(value = "select * from jcm_offer_details where jcm_id in (select job_candidate_mapping.id \n" +
            "from job_candidate_mapping where job_id in (select job.id from job where company_id =:id))", nativeQuery = true)
    List<JcmOfferDetails> findByCompanyId(Long id);
}
