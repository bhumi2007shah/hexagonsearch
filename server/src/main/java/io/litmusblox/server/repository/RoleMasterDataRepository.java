/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.FunctionMasterData;
import io.litmusblox.server.model.RoleMasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author : Sumit
 * Date : 03/04/20
 * Time : 6:36 PM
 * Class Name : RoleMasterDataRepository
 * Project Name : server
 */
@Repository
public interface RoleMasterDataRepository extends JpaRepository<RoleMasterData, Long> {

    List<RoleMasterData> findByFunction(FunctionMasterData function);
}
