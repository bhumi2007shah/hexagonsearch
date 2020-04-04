/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.RejectionReasonMasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author : Sumit
 * Date : 19/03/20
 * Time : 1:12 PM
 * Class Name : RejectionReasonMasterDataRepository
 * Project Name : server
 */
@Repository
public interface RejectionReasonMasterDataRepository extends JpaRepository<RejectionReasonMasterData, Long> {
}
