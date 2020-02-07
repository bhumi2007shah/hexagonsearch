/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.Company;
import io.litmusblox.server.model.User;
import io.litmusblox.server.repository.CompanyRepository;
import io.litmusblox.server.repository.CustomQueryExecutor;
import io.litmusblox.server.service.AnalyticsResponseBean;
import io.litmusblox.server.service.IAnalyticsService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service interface for all analytics related queries
 *
 * @author : Shital Raval
 * Date : 5/2/20
 * Time : 1:47 PM
 * Class Name : AnalyticsService
 * Project Name : server
 */
@Service
@Log4j2
public class AnalyticsService implements IAnalyticsService {

    @Resource
    CompanyRepository companyRepository;

    @Autowired
    CustomQueryExecutor customQueryExecutor;

    /**
     * Find analytics
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws Exception
     */
    @Override
    public List<AnalyticsResponseBean> analyticsByCompany(Date startDate, Date endDate) throws Exception {
        log.info("Received request to find analytics");
        long startTime = System.currentTimeMillis();
        List<Long> companyIds = new ArrayList<>();
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (IConstant.UserRole.SUPER_ADMIN.toString().equals(loggedInUser.getRole()))
            companyIds = companyRepository.findAll().stream()
                    .map(Company::getId).collect(Collectors.toList());
        else
            companyIds.add(loggedInUser.getCompany().getId());
        List<AnalyticsResponseBean> responseBeans = customQueryExecutor.analyticsByCompany(startDate, endDate, StringUtils.join(companyIds,","));
        log.info("Completed request to find analytics in {} ms.", System.currentTimeMillis()-startTime);
        return responseBeans;
    }
}
