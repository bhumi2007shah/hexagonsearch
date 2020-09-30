/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JcmProfileSharingMaster;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository class for JcmProfileSharingMaster
 *
 * @author : Shital Raval
 * Date : 9/8/19
 * Time : 1:45 PM
 * Class Name : JcmProfileSharingMasterRepository
 * Project Name : server
 */
public interface JcmProfileSharingMasterRepository extends JpaRepository<JcmProfileSharingMaster, Long> {
}
