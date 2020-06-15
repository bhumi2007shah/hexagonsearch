/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model.client;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : sameer
 * Date : 29/05/20
 * Time : 12:27 PM
 * Class Name : LevelScore
 * Project Name : server
 */
@Data
public class LevelScore {

    @NotNull
    private String type;

    @NotNull
    private Long count;
}
