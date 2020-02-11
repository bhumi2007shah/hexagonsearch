/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.InterviewerDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.Resource;

/**
 * @author : Sumit
 * Date : 06/02/20
 * Time : 2:26 PM
 * Class Name : InterviewerDetailsRepository
 * Project Name : server
 */
@Resource
public interface InterviewerDetailsRepository extends JpaRepository<InterviewerDetails, Long> {
}
