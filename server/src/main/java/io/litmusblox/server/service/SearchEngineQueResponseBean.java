/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

/**
 * @author : oem
 * Date : 29/04/20
 * Time : 2:25 PM
 * Class Name : TechQueResponseBean
 * Project Name : server
 */
@Data
public class SearchEngineQueResponseBean {
    private String questionText;
    private String questionType;
    private String[]  options;
}
