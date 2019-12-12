/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.model.Job;
import io.litmusblox.server.service.IJobService;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author : shital
 * Date : 7/11/19
 * Time : 11:48 AM
 * Class Name : JobControllerMvcTest
 * Project Name : server
 */
@ExtendWith(MockitoExtension.class)
@RunWith(SpringRunner.class)
@Log4j2
public class JobControllerTest {
    @InjectMocks
    JobController jobController;

    @Mock
    IJobService jobService;

    @Test
    public void addJob() {
        String jobRequest = "{\n" +
                "\"jobTitle\":\"Java Developer\",\n" +
                "\"noOfPositions\":2,\n" +
                "\"education\": {\"id\": \"20\"},\n" +
                "\"experienceRange\": {\"id\": \"120\"},\n" +
                "\"expertise\": {\"id\": 117},\n" +
                "\"function\": {\"id\": \"3\"},\n" +
                "\"jobDescription\":\"We are looking for highly skilled programmers with experience building web applications in Java. Java Developers are responsible for analyzing user requirements and business objectives, determining application features and functionality and recommending changes to existing Java-based applications, among other duties.\n" +
                "Java Developers need to compile detailed technical documentation and user assistance material, requiring excellent written communication.\n" +
                "Java Developer Responsibilities:\n" +
                "Designing and implementing Java-based applications.\n" +
                "Analyzing user requirements to inform application design.\n" +
                "Defining application objectives and functionality.\n" +
                "Aligning application design with business goals.\n" +
                "Developing and testing software.\n" +
                "Debugging and resolving technical problems that arise.\n" +
                "Producing detailed design documentation.\n" +
                "Recommending changes to existing Java infrastructure.\n" +
                "Developing multimedia applications.\n" +
                "Developing documentation to assist users.\n" +
                "Ensuring continuous professional self-development.\n" +
                "Java Developer Requirements:\n" +
                "Degree in Computer Science or related field.\n" +
                "Experience with user interface design, database structures and statistical analyses.\n" +
                "Analytical mindset and good problem-solving skills.\n" +
                "Excellent written and verbal communication.\n" +
                "Good organizational skills.\n" +
                "Ability to work as part of a team.\n" +
                "Attention to detail.\"\n" +
                "}";
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Job job = Job.builder().id(1L).build();

        boolean testPass = true;
        try {
            when(jobService.addJob(any(Job.class), any(String.class))).thenReturn(job);

            String response = jobController.addJob(jobRequest,"overview");
            assertThat(response).isNotNull();
            log.info(response);
            assertThat(response.indexOf("id")).isGreaterThan(0);

        } catch(Exception e) {
            e.printStackTrace();
            testPass = false;
        }
        assertThat(testPass).isTrue();
    }

}
