/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

import java.util.List;

/**
 * @author : oem
 * Date : 29/04/20
 * Time : 2:25 PM
 * Class Name : TechQueResponseBean
 * Project Name : server
 */
@Data
public class SearchEngineQuestionsResponseBean {
    private String questionText;
    private String questionType;
    private String[]  options;
    private List<String> defaultAnswers;
    private String scoringType;
    private String answerSelection;
}
