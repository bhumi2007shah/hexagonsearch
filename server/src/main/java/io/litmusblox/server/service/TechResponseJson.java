/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

import java.util.List;

/**
 * @author : sameer
 * Date : 18/02/20
 * Time : 4:24 PM
 * Class Name : TechResponseJson
 * Project Name : server
 */
@Data
public class TechResponseJson {
    private String name;
    private List<Complexity> complexities;
    private Long score;
    private Long capabilityStarRating;
}

@Data
class Complexity{
    private String name;
    private String jobScenario;
    private String candidateScenario;
    private Float match;
    private Long score;
}
