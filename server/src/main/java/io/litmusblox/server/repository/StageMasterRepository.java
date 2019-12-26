/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.StageMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author : Shital Raval
 * Date : 19/11/19
 * Time : 1:49 PM
 * Class Name : StageMasterRepository
 * Project Name : server
 */
public interface StageMasterRepository extends JpaRepository<StageMaster, Long> {
    List<StageMaster> findAllByOrderByIdAsc();
}
