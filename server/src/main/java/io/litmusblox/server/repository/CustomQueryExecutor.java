/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Job;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : Shital Raval
 * Date : 27/12/19
 * Time : 10:37 AM
 * Class Name : CustomQueryExecutor
 * Project Name : server
 */
@Service
public class CustomQueryExecutor {
    @PersistenceContext
    EntityManager entityManager;

    @Resource
    JobRepository jobRepository;

    public List<Job> executeSearchQuery(String queryString) {
        List<Integer> jobIds = entityManager.createNativeQuery(queryString).getResultList();
        List<Long> resultSet  = jobIds.stream()
                .mapToLong(Integer::longValue)
                .boxed().collect(Collectors.toList());
        List<Job> jobs = jobRepository.findAllById(resultSet);
        return jobs;
    }
}
