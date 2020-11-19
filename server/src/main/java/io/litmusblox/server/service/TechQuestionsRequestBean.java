/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
public class TechQuestionsRequestBean {
    private Long companyId;
    private SelectedRoles selectedRoles;
    private Industry industry;
    private Functions functions;
    private List<String> skills;

    @Data
    public static class SelectedRoles {
        private List<String> roleNames = new ArrayList<>();
    }

    @Data
    public static class Industry {
        private String industryName;
    }

    @Data
    public static class Functions {
        private List<String> functionNames = new ArrayList<>();
    }

}




