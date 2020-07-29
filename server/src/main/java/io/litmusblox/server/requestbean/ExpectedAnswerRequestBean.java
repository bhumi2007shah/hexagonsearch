/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.requestbean;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author : sameer
 * Date : 06/07/20
 * Time : 3:44 PM
 * Class Name : ExpectedAnswerRequestBean
 * Project Name : server
 */
@Data
public class ExpectedAnswerRequestBean {
    private Long id;
    private List<Map<Object, Object>> expectedAnswer;
}
