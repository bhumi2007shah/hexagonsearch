/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.ExportFormatDetail;
import io.litmusblox.server.model.ExportFormatMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : sameer
 * Date : 03/12/19
 * Time : 12:57 PM
 * Class Name : ExportFormatDetailRepository
 * Project Name : server
 */
public interface ExportFormatDetailRepository extends JpaRepository<ExportFormatDetail, Long> {
    @Transactional(readOnly = true)
    List<ExportFormatDetail> findByExportFormatMasterOrderByPositionAsc(ExportFormatMaster exportFormatMaster);
}
