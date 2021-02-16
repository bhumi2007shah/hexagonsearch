/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;
import io.litmusblox.server.model.JcmOfferDetails;
import io.litmusblox.server.model.JobCandidateMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JcmOfferDetailsRepository extends JpaRepository<JcmOfferDetails,Long> {
    @Transactional(readOnly = true)
    JcmOfferDetails findByJcmId(JobCandidateMapping jcmId);
}
