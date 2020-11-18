/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ScreeningQuestionRequestBean {
    private Map<Long, List<String>> screeningQuestionResponseMap = new HashMap<>();
    private Map<String, String> quickScreeningQuestionResponseMap = new HashMap<>();
}
