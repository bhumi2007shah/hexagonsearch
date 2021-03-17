/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.CreateJobPageSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author : Shital Raval
 * Date : 30/9/19
 * Time : 10:25 AM
 * Class Name : CreateJobPageSequenceRepository
 * Project Name : server
 */
@Repository
public interface CreateJobPageSequenceRepository extends JpaRepository<CreateJobPageSequence, Long> {
    List<CreateJobPageSequence> findByDisplayFlagIsTrueAndSubscriptionAvailabilityOrderByPageDisplayOrderAsc(String subscription);
}
