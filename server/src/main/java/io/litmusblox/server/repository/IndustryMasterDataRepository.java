/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.IndustryMasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author : Sumit
 * Date : 03/04/20
 * Time : 6:35 PM
 * Class Name : IndustryMasterDataRepository
 * Project Name : server
 */
@Repository
public interface IndustryMasterDataRepository extends JpaRepository<IndustryMasterData, Long> {
}
