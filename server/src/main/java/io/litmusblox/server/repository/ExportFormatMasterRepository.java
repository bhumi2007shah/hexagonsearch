/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.ExportFormatMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : sameer
 * Date : 03/12/19
 * Time : 12:55 PM
 * Class Name : ExportFormatMaster
 * Project Name : server
 */
public interface ExportFormatMasterRepository extends JpaRepository<ExportFormatMaster, Long> {
    @Transactional
    @Query(nativeQuery = true, value = "select * from export_format_master where company_id is null and system_supported=true")
    List<ExportFormatMaster> exportDefaultFormatMasterList();

    List<ExportFormatMaster> findByCompanyIdAndSystemSupportedIsTrue(Long companyId);
}
