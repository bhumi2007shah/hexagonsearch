/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.uploadProcessor;

import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.User;

/**
 * @author : Shital Raval
 * Date : 2/12/19
 * Time : 11:49 AM
 * Class Name : HtmlParser
 * Project Name : server
 */
public interface HtmlParser {
    Candidate parseData(String htmlData, User createdBy);
}
