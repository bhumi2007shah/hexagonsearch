/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author : sameer
 * Date : 29/05/20
 * Time : 12:26 PM
 * Class Name : EmployeeDetail
 * Project Name : server
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeDetail {

    @NotNull
    private String competency;

    @NotNull
    private String subCompetencyCount;

    private List<LevelScore> scoreSummary;

    private List<SubCompetencyDetail> subCompetencies;

}
