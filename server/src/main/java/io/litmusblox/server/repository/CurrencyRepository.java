/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.repository;

import io.litmusblox.server.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author : oem
 * Date : 05/11/19
 * Time : 3:37 PM
 * Class Name : CurrencyRepository
 * Project Name : server
 */
@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Currency findByCurrencyShortName(String currencyShortName);
}
