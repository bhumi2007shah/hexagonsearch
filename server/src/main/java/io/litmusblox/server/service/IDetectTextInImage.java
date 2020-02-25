/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import java.net.URL;

/**
 * @author : sameer
 * Date : 25/01/20
 * Time : 2:56 PM
 * Class Name : DetectTextInImage
 * Project Name : server
 */
public interface IDetectTextInImage {
    String detectText(URL imageUrl) throws Exception;
}
