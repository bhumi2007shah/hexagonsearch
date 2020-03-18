/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.AsyncOperationsErrorRecords;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : shital
 * Date : 25/2/20
 * Time : 1:03 PM
 * Class Name : AsyncOperationsErrorRecordsRepository
 * Project Name : server
 */
public interface AsyncOperationsErrorRecordsRepository extends JpaRepository<AsyncOperationsErrorRecords, Long> {
}
