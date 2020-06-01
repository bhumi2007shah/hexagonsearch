/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model.client;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : sameer
 * Date : 29/05/20
 * Time : 12:37 PM
 * Class Name : Score
 * Project Name : server
 */
@Data
public class Score {
    @NotNull
    private String type;
}
