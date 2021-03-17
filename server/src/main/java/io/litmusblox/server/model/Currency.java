/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import lombok.Data;

import javax.persistence.*;

/**
 * @author : Sumit
 * Date : 05/11/19
 * Time : 3:00 PM
 * Class Name : Currency
 * Project Name : server
 */
@Data
@Entity
@Table(name = "CURRENCY")
public class Currency {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CURRENCY_FULL_NAME")
    private String currencyFullName;

    @Column(name = "CURRENCY_SHORT_NAME")
    private String currencyShortName;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "MIN_SALARY")
    private Long minSalary;

    @Column(name = "MAX_SALARY")
    private Long maxSalary;

    @Column(name = "SALARY_UNIT")
    private String salaryUnit;
}
