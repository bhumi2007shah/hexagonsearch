/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public CvRatingRequestBean(Map<String, List<String>> neighbourSkillMap) {
        this.jdKeySkillsMap = neighbourSkillMap;
    }
}
