/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server;

import io.litmusblox.server.service.IMasterDataService;
import io.litmusblox.server.service.MasterDataBean;
import io.litmusblox.server.service.impl.LbUserDetailsService;
import lombok.extern.log4j.Log4j2;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : Shital Raval
 * Date : 4/11/19
 * Time : 11:04 AM
 * Class Name : AbstractTest
 * Project Name : server
 */
@RunWith(SpringRunner.class)
@Log4j2
@ActiveProfiles("test")
public class AbstractTest {

    @Autowired
    IMasterDataService masterDataService;

    @Autowired
    LbUserDetailsService lbUserDetailsService;

    public static Authentication authentication = null;

    @Before
    @Transactional
    public final void setUp() {

        try {
            if (!MasterDataBean.getInstance().isLoaded()) {
                masterDataService.loadStaticMasterData();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.fatal("Failed to load default Configuration.");
        }
    }

    @BeforeEach
    void setApplicationContext() {
        try {
            UserDetails userDetails = this.lbUserDetailsService.loadUserByUsername("test@litmusblox.io");

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        authentication = SecurityContextHolder.getContext().getAuthentication();
    }

}
