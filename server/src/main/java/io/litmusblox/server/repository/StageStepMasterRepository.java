/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.StageStepMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Sumit
 * Date : 02/02/20
 * Time : 8:24 PM
 * Class Name : StageStepMasterRepository
 * Project Name : server
 */
public interface StageStepMasterRepository extends JpaRepository<StageStepMaster, Long> {

    @Transactional
    List<StageStepMaster> findAllByOrderByIdAsc();

    @Transactional
    StageStepMaster findByStage(String stage);
}
