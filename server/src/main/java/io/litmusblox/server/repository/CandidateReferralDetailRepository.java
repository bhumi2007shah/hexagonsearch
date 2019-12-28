/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.CandidateReferralDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author : Sumit
 * Date : 27/12/19
 * Time : 8:17 PM
 * Class Name : CandidateReferralDetailRepository
 * Project Name : server
 */
@Repository
public interface CandidateReferralDetailRepository extends JpaRepository<CandidateReferralDetail, Long> {
}
