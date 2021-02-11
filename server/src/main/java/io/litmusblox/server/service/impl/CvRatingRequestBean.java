/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @author : Shital Raval
 * Date : 18/10/19
 * Time : 12:01 PM
 * Class Name : MlCvRatingRequestBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CvRatingRequestBean {
    Map<String, List<String>> jdKeySkillsMap = new HashMap<>();
    List<String> resumeContentSkills = new ArrayList<>();
    String industry = "";
    Set<String> skillSet = new HashSet<>();

    public CvRatingRequestBean(Map<String, List<String>> neighbourSkillMap, Set<String> skillSet) {
        this.jdKeySkillsMap = neighbourSkillMap;
        this.skillSet = skillSet;
    }
}
