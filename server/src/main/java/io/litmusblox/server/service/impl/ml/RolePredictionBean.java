/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl.ml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : Sumit
 * Date : 16/10/19
 * Time : 11:44 AM
 * Class Name : RolePredictionBean
 * Project Name : server
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolePredictionBean {
    private RolePrediction rolePrediction;

    @Data
    public static class RolePrediction {
        private String jobTitle;
        private String jobDescription;
        private String recruiterRoles;
        private String industry;
    }
}


