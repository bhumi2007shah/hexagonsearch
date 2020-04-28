/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.FunctionMasterData;
import io.litmusblox.server.model.IndustryMasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author : Sumit
 * Date : 03/04/20
 * Time : 6:36 PM
 * Class Name : FunctionMasterDataRepository
 * Project Name : server
 */
@Repository
public interface FunctionMasterDataRepository extends JpaRepository<FunctionMasterData, Long> {

    List<FunctionMasterData> findByIndustry(IndustryMasterData industry);
}
