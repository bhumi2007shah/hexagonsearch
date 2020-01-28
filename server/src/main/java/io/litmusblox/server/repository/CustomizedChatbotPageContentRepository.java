/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Company;
import io.litmusblox.server.model.CustomizedChatbotPageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : oem
 * Date : 27/01/20
 * Time : 4:29 PM
 * Class Name : CustomizedChatbotPageContentRepository
 * Project Name : server
 */
@Repository
public interface CustomizedChatbotPageContentRepository extends JpaRepository<CustomizedChatbotPageContent, Long> {

    @Transactional
    CustomizedChatbotPageContent findByCompanyId(Company companyId);

}
