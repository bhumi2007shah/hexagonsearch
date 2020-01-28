/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLHStoreType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Sumit
 * Date : 27/01/20
 * Time : 4:00 PM
 * Class Name : CustomizedChatbotPageContent
 * Project Name : server
 */
@Data
@Entity
@Table(name = "CUSTOMIZED_CHATBOT_PAGE_CONTENT")
@TypeDef(name = "hstore", typeClass = PostgreSQLHStoreType.class)
public class CustomizedChatbotPageContent {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID")
    private Company companyId;

    @Type(type="hstore")
    @Column(name = "PAGE_INFO", columnDefinition = "hstore")
    private Map<String, String> pageInfo = new HashMap<>();

}
