/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.AbstractTest;
import io.litmusblox.server.model.User;
import io.litmusblox.server.service.impl.LbUserDetailsService;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author : sameer
 * Date : 18/09/19
 * Time : 10:29 AM
 * Class Name : LbUserDetailsServiceTest
 * Project Name : server
 */
@ActiveProfiles("test")
@NoArgsConstructor
@RunWith(SpringRunner.class)
@SpringBootTest
@Log4j2
class LbUserDetailsServiceTest extends AbstractTest {

    @Autowired
    LbUserDetailsService lbUserDetailsService;


    @org.junit.jupiter.api.Test
    void login() {
        boolean testPass = true;
        try {
            User user = new User();

            user.setEmail("test@litmusblox.io");
            user.setPassword("123456");

            LoginResponseBean loginResponseBean = lbUserDetailsService.login(user);
            assertThat(loginResponseBean).isNotNull();
            assertThat(loginResponseBean.getCompany()).isNotNull();
        } catch (Exception e) {
            e.printStackTrace();
            testPass = false;
        }
        assertThat(testPass).isTrue();
    }
}
