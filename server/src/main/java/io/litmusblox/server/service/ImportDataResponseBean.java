/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
@Data
public class ImportDataResponseBean {
    private Set<String> skillSet = new HashSet<>();
    private Map<String, String> errorResponse = new HashMap<>();
}
