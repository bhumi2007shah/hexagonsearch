/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.StepsPerStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author : Shital Raval
 * Date : 20/11/19
 * Time : 1:27 PM
 * Class Name : StepsPerStageRepository
 * Project Name : server
 */
public interface StepsPerStageRepository extends JpaRepository<StepsPerStage, Long> {
    List<StepsPerStage> findAllByOrderByIdAsc();
}
