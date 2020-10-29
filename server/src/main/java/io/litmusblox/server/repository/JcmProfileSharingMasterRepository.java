/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.JcmProfileSharingMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
