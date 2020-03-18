/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.CvParsingApiDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Sumit
 * Date : 17/01/20
 * Time : 11:46 AM
 * Class Name : CvParsingApiDetailsRepository
 * Project Name : server
 */
@Repository
public interface CvParsingApiDetailsRepository extends JpaRepository<CvParsingApiDetails, Long> {

    @Transactional(readOnly = true)
    List<CvParsingApiDetails> findAllByActiveOrderByApiSequenceAsc(boolean isActive);

    @Transactional(readOnly = true)
    CvParsingApiDetails findByColumnToUpdate(String columnToUpdate);
}
