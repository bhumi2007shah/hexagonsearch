/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model.client;

import lombok.Data;

import java.util.List;

/**
 * @author : sameer
 * Date : 01/06/20
 * Time : 10:36 AM
 * Class Name : CompetencyReuirements
 * Project Name : server
 */
@Data
public class CompetencyRequirements {
    private Long requiredCount;
    List<String> list;
}
