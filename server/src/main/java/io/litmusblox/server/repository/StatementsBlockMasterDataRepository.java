/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.StatementsBlockMasterData;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.Resource;

@Resource
public interface StatementsBlockMasterDataRepository extends JpaRepository<StatementsBlockMasterData, Long> {
}
