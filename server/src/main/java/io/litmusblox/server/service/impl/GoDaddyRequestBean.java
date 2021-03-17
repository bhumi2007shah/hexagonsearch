/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : Shital Raval
 * Date : 17/1/20
 * Time : 1:26 PM
 * Class Name : GoDaddyRequestBean
 * Project Name : server
 */
@Data
@NoArgsConstructor
public class GoDaddyRequestBean {
    String name;
    String type = "A";
    String data;
    int ttl = 3600;

    public GoDaddyRequestBean(String name, String data) {
        this.name = name;
        this.data = data;
    }
}
