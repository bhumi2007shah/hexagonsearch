/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.responsebean.export.JcmExportResponseBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author : sameer
 * Date : 10/06/20
 * Time : 8:05 PM
 * Class Name : JcmExportResponseBeanRepository
 * Project Name : server
 */
public interface JcmExportResponseBeanRepository extends JpaRepository<JcmExportResponseBean, Long> {
    @Query(nativeQuery = true, value = "select * from export_data_view where job_id=:jobId and current_stage=:stage")
    List<JcmExportResponseBean> findAllByJobIdAndStage(Long jobId, String stage);
}
