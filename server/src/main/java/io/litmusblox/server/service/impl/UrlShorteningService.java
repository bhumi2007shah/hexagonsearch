/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.ShortUrl;
import io.litmusblox.server.repository.ShortUrlRepository;
import io.litmusblox.server.service.IUrlShorteningService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author : sameer
 * Date : 13/03/20
 * Time : 4:54 PM
 * Class Name : UrlShorteningService
 * Project Name : server
 */
@Service
@Log4j2
public class UrlShorteningService implements IUrlShorteningService {

    @Autowired
    ShortUrlRepository shortUrlRepository;

    @Autowired
    Environment environment;

    /**
     * Function to handle get request for short url.
     * @param hash - path variable appended in short url.
     * @return Redirect Modal and View to long url if exist else throws WebException with message
     * "Invalid URL" and error_code - BAD_REQUEST
     */
    @Override
    public ModelAndView findByHash(String hash) {
        log.info("Received request for short url {}{}", environment.getProperty("shortServerUrl"), hash);
        long startTime = System.currentTimeMillis();
        ShortUrl shortUrl = shortUrlRepository.findByHash(hash);
        if(null!=shortUrl && shortUrl.getUrl()!=null) {
            log.info("Completed request for short url in {}", System.currentTimeMillis()-startTime);
            return new ModelAndView(new RedirectView(shortUrl.getUrl()));
        }
        else{
            log.error("Short url not found: {}{}", environment.getProperty("shortServerUrl"), hash);
            throw new WebException("Invalid URL", HttpStatus.BAD_REQUEST);
        }
    }
}
