/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author : sameer
 * Date : 13/03/20
 * Time : 4:57 PM
 * Class Name : ShortUrlRepository
 * Project Name : server
 */
@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Integer> {
    public ShortUrl findByHash(String hash);
}
