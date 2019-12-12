/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.litmusblox.server.AbstractTest;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.Company;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.MasterData;
import io.litmusblox.server.model.User;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test case for JobService
 *
 * @author : Shital Raval
 * Date : 7/11/19
 * Time : 1:42 PM
 * Class Name : JobServiceTest
 * Project Name : server
 */
@ActiveProfiles("test")
@NoArgsConstructor
@RunWith(SpringRunner.class)
@SpringBootTest
@Log4j2
class JobServiceTest extends AbstractTest {
    @Autowired
    IJobService jobService;

    WireMockServer wireMockServer = new WireMockServer();

    @BeforeEach
    void setupWireMockServer() {
        wireMockServer.start();
        configureFor("localhost", 8080);
        stubFor(post(urlMatching("/api/predictRole"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(mlPredictorResponse)));

    }

    @AfterEach
    void stopWireMockServer() {
        wireMockServer.stop();
    }

    static String mlPredictorResponse = "{\n" +
            "    \"skills\": [\n" +
            "        {\n" +
            "            \"name\": \"requirements\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"software development\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"product releases\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"testing\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"releases\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"database architecture\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"spring\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"software development life cycle\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"java\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"agile methodologies\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"agile\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"mvc\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"tasks\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"roles\": [\n" +
            "        {\n" +
            "            \"score\": 31,\n" +
            "            \"role\": \"Backend Developer\",\n" +
            "            \"keywords\": \"Java 9 time(s)  software development 3 time(s)  spring 1 time(s)\",\n" +
            "            \"percentage\": 43.055553\n" +
            "        },\n" +
            "        {\n" +
            "            \"score\": 23,\n" +
            "            \"role\": \"Agile Scrum Master\",\n" +
            "            \"keywords\": \"software development 3 time(s)  Agile methodologies 1 time(s)  software development life cycle 3 time(s)  tasks 2 time(s)\",\n" +
            "            \"percentage\": 31.944445\n" +
            "        },\n" +
            "        {\n" +
            "            \"score\": 18,\n" +
            "            \"role\": \"Bigdata Engineer\",\n" +
            "            \"keywords\": \"Java 9 time(s)\",\n" +
            "            \"percentage\": 25\n" +
            "        }\n" +
            "    ],\n" +
            "    \"suggestedCapabilities\": [\n" +
            "        {\n" +
            "            \"capability\": \"Server Side scripting\",\n" +
            "            \"capabilityWeight\": 10,\n" +
            "            \"id\": \"10115\",\n" +
            "            \"capScore\": 12.550608\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Middleware Services\",\n" +
            "            \"capabilityWeight\": 8,\n" +
            "            \"id\": \"10129\",\n" +
            "            \"capScore\": 10.040485\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Database Access Layer Programming\",\n" +
            "            \"capabilityWeight\": 8,\n" +
            "            \"id\": \"10130\",\n" +
            "            \"capScore\": 10.040485\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Programming Basics\",\n" +
            "            \"capabilityWeight\": 6,\n" +
            "            \"id\": \"10109\",\n" +
            "            \"capScore\": 7.5303645\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Application & Solution Design\",\n" +
            "            \"capabilityWeight\": 6,\n" +
            "            \"id\": \"10053\",\n" +
            "            \"capScore\": 7.5303645\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Data Engineering\",\n" +
            "            \"capabilityWeight\": 10,\n" +
            "            \"id\": \"10148\",\n" +
            "            \"capScore\": 7.2874494\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Data management basics\",\n" +
            "            \"capabilityWeight\": 10,\n" +
            "            \"id\": \"10146\",\n" +
            "            \"capScore\": 7.2874494\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Data Processing\",\n" +
            "            \"capabilityWeight\": 10,\n" +
            "            \"id\": \"10147\",\n" +
            "            \"capScore\": 7.2874494\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"ETL Programming\",\n" +
            "            \"capabilityWeight\": 8,\n" +
            "            \"id\": \"10063\",\n" +
            "            \"capScore\": 5.8299594\n" +
            "        }\n" +
            "    ],\n" +
            "    \"additionalCapabilities\": [\n" +
            "        {\n" +
            "            \"capability\": \"Programming Business Logic\",\n" +
            "            \"capabilityWeight\": 8,\n" +
            "            \"id\": \"10128\",\n" +
            "            \"capScore\": 5.8299594\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Machine Learning Programming\",\n" +
            "            \"capabilityWeight\": 6,\n" +
            "            \"id\": \"10149\",\n" +
            "            \"capScore\": 4.37247\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"BI Programming\",\n" +
            "            \"capabilityWeight\": 4,\n" +
            "            \"id\": \"10062\",\n" +
            "            \"capScore\": 2.9149797\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Handling Client Interactions\",\n" +
            "            \"capabilityWeight\": 2,\n" +
            "            \"id\": \"10071\",\n" +
            "            \"capScore\": 2.5101213\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Estimation\",\n" +
            "            \"capabilityWeight\": 2,\n" +
            "            \"id\": \"10070\",\n" +
            "            \"capScore\": 2.5101213\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Agile Team Member\",\n" +
            "            \"capabilityWeight\": 2,\n" +
            "            \"id\": \"10132\",\n" +
            "            \"capScore\": 2.5101213\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Build & Deploy\",\n" +
            "            \"capabilityWeight\": 2,\n" +
            "            \"id\": \"10131\",\n" +
            "            \"capScore\": 2.5101213\n" +
            "        },\n" +
            "        {\n" +
            "            \"capability\": \"Database Administration - DBA\",\n" +
            "            \"capabilityWeight\": 2,\n" +
            "            \"id\": \"10047\",\n" +
            "            \"capScore\": 1.4574898\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    @org.junit.jupiter.api.Test
    void addJob() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        boolean testPass = true;
        try {
            Job testJob = Job.builder()
                    .jobTitle("job title")
                    .jobDescription("job description")
                    .noOfPositions(1)
                    .mlDataAvailable(false)
                    .createdBy(User.builder().id(1L).build())
                    .createdOn(new Date())
                    .companyId(Company.builder().id(2L).build())
                    .education(MasterData.builder().id(15L).build())
                    .experienceRange(MasterData.builder().id(125L).build())
                    .expertise(MasterData.builder().id(122L).build())
                    .function(MasterData.builder().id(130L).build())
                    .currency("INR")
                    .usersForCompany(new ArrayList<>())
                    .build();
            jobService.addJob(testJob, IConstant.AddJobPages.overview.name());
            assertThat(testJob.getId()).isNotNull();
        } catch (Exception e) {
            e.printStackTrace();
            testPass = false;
        }

        assertThat(testPass).isTrue();
    }
}