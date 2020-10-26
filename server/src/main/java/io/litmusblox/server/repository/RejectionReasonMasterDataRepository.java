/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.RejectionReasonMasterData;
import io.litmusblox.server.model.StageStepMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author : Sumit
 * Date : 19/03/20
 * Time : 1:12 PM
 * Class Name : RejectionReasonMasterDataRepository
 * Project Name : server
 */
@Repository
public interface RejectionReasonMasterDataRepository extends JpaRepository<RejectionReasonMasterData, Long> {
    List<RejectionReasonMasterData> findByStageId(StageStepMaster stageId);
}
