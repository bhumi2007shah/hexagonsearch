/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import org.springframework.web.servlet.ModelAndView;

/**
 * @author : sameer
 * Date : 13/03/20
 * Time : 4:51 PM
 * Class Name : IUrlShorteningService
 * Project Name : server
 */
public interface IUrlShorteningService {
    /**
     *
     * @param hash
     * @return return a Response Entity with Redirect model and view to long url  if record for short
     * url is found alse returns 400
     */
    public ModelAndView findByHash(String hash);
}
