/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model.client;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author : sameer
 * Date : 29/05/20
 * Time : 12:28 PM
 * Class Name : CompetencyDetail
 * Project Name : server
 */
@Data
public class SubCompetencyDetail {

    @NotNull
    private String subCompetency;

    @NotNull
    private Score score;

    @NotNull
    private String response;

    private List<EmployeeResponse> responses;

    private String requirement;

    private CompetencyRequirements requirements;

}
