/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JdParserResponseBean {

    private Map<String, List<SearchEngineQuestionsResponseBean>> questionMap = new HashMap<>();
    private Map<String, List<String>> neighbourSkillMap = new HashMap<>();
}
