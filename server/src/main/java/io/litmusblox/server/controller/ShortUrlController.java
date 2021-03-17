/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.controller;

import io.litmusblox.server.service.IUrlShorteningService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author : sameer
 * Date : 13/03/20
 * Time : 4:34 PM
 * Class Name : ShortUrlController
 * Project Name : server
 */
@RestController
@RequestMapping("/")
@Log4j2
public class ShortUrlController {
    @Autowired
    IUrlShorteningService urlShorteningService;

    @GetMapping("{hash}")
    public ModelAndView redirect(@PathVariable("hash")String hash)throws Exception{
        return urlShorteningService.findByHash(hash);
    }
}
