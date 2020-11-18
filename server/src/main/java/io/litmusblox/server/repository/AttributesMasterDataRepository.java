/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.AttributesMasterData;
import io.litmusblox.server.model.FunctionMasterData;
import io.litmusblox.server.model.RoleMasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributesMasterDataRepository extends JpaRepository<AttributesMasterData, Long> {
    List<AttributesMasterData> findByFunction(FunctionMasterData function);
}
