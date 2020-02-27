/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.Candidate;
import lombok.Data;

import java.util.List;

/**
 * @author : sameer
 * Date : 18/02/20
 * Time : 4:33 PM
 * Class Name : TechRoleCompetencyBean
 * Project Name : server
 */

@Data
public class TechRoleCompetencyBean {
    private Candidate candidate;
    private List<TechResponseJson> techResponseJson;
    private String candidateProfileLink;
}
