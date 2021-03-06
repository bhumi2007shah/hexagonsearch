/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.InterviewDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * @author : Sumit
 * Date : 06/02/20
 * Time : 2:37 PM
 * Class Name : InterviewDetailsRepository
 * Project Name : server
 */
@Resource
public interface InterviewDetailsRepository extends JpaRepository<InterviewDetails, Long> {

    @Transactional(readOnly = true)
    List<InterviewDetails> findByIdIn(List<Long> interviewIdList);

    @Transactional(readOnly = true)
    InterviewDetails findByInterviewReferenceId(UUID interviewReferenceId);
}
