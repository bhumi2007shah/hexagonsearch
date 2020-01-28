/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.JobCandidateMapping;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Sumit
 * Date : 27/01/20
 * Time : 3:53 PM
 * Class Name : ChatbotResponseBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
public class ChatbotResponseBean {

    private JobCandidateMapping jobCandidateMapping;
    private Map<String, String> chatbotContent = new HashMap<>();
}
