/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.AbstractTest;
import io.litmusblox.server.model.UserScreeningQuestion;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test case for MasterDataService
 *
 * @author : Shital Raval
 * Date : 5/7/19
 * Time : 6:42 PM
 * Class Name : MasterDataServiceTest
 * Project Name : server
 */
@ActiveProfiles("test")
@NoArgsConstructor
@RunWith(SpringRunner.class)
@SpringBootTest
@Log4j2
class MasterDataServiceTest extends AbstractTest {
    @Autowired
    IMasterDataService masterDataService;

    @org.junit.jupiter.api.Test
    void fetchForItems() {
        try {
            MasterDataResponse responseObj = masterDataService.fetchForItems(Arrays.asList(new String[]{"countries"}));
            assertThat(responseObj).isNotNull();
            assertThat(responseObj.getCountries()).isNotNull();
            log.info("No. of countries = " + responseObj.getCountries().size());
            assertThat(responseObj.getCountries().size() > 0).isTrue();
        } catch (Exception e) {

        }
    }

    @org.junit.jupiter.api.Test
    void addMasterData() {
        boolean testPass = true;
        try {
            String jsonData = "{\n" +
                    "\t\"question\":\"What versions of angular have you worked on?\",\n" +
                    "\t\"questionType\":{\n" +
                    "\t\t\"id\":97\n" +
                    "\t},\n" +
                    "\t\"options\":[\"Angular 1\", \"Angular 2\", \"Angular 4\"],\n" +
                    "\t\"userId\": {\n" +
                    "\t\t\"id\":1\n" +
                    "\t}\n" +
                    "}";
            masterDataService.addMasterData(jsonData, UserScreeningQuestion.IDENTIFIER);
        } catch (Exception e) {
            e.printStackTrace();
            testPass = false;
        }

        assertThat(testPass).isTrue();
    }

    @org.junit.jupiter.api.Test
    void addMasterDataTextType() {
        boolean testPass = true;
        try {
            String jsonData = "{\n" +
                    "\t\"question\":\"What is your preferred city?\",\n" +
                    "\t\"questionType\":{\n" +
                    "\t\t\"id\":99\n" +
                    "\t},\n" +
                    "\t\"userId\": {\n" +
                    "\t\t\"id\":1\n" +
                    "\t}\n" +
                    "}";
            masterDataService.addMasterData(jsonData, UserScreeningQuestion.IDENTIFIER);
        } catch (Exception e) {
            e.printStackTrace();
            testPass = false;
        }

        assertThat(testPass).isTrue();
    }
}