/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;
import io.litmusblox.server.model.JcmOfferDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JcmOfferDetailsRepository extends JpaRepository<JcmOfferDetails,Long> {
    @Query(value = "select * from jcm_offer_details where jcm_id =:jcmId" ,nativeQuery = true)
    JcmOfferDetails findByJcmId(Long jcmId);
}
