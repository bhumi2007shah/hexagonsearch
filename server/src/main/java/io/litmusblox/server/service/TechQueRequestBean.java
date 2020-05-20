/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : Sumit
 * Date : 28/04/20
 * Time : 3:08 PM
 * Class Name : TechQueRequestBean
 * Project Name : server
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechQueRequestBean {
    private Long companyId;
    private SelectedRole selectedRole;
    private Industry industry;
    private Function function;
    private List<String> skills;

    @Data
    public static class SelectedRole {
        private String roleName;
    }

    @Data
    public static class Industry {
        private String industryName;
    }

    @Data
    public static class Function {
        private String functionName;
    }

}




