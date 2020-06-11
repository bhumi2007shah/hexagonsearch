/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.responsebean.export.JcmExportQAResponseBean;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author : sameer
 * Date : 10/06/20
 * Time : 8:46 PM
 * Class Name : JcmExportQAResponseBeanRepository
 * Project Name : server
 */
public interface JcmExportQAResponseBeanRepository extends JpaRepository<JcmExportQAResponseBean, Long> {
    List<JcmExportQAResponseBean> findAllByJcmIdOrderByJsqIdAsc(Long jcmId);
}
