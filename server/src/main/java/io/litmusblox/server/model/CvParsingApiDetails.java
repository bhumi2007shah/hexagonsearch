/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLHStoreType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Sumit
 * Date : 17/01/20
 * Time : 11:22 AM
 * Class Name : CvParsingApiDetails
 * Project Name : server
 */
@Data
@Entity
@Table(name = "CV_PARSING_API_DETAILS")
@TypeDef(name = "hstore", typeClass = PostgreSQLHStoreType.class)
@NoArgsConstructor
public class CvParsingApiDetails {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "API_URL")
    private String apiUrl;

    @Column(name = "API_SEQUENCE")
    private Integer apiSequence;

    @Column(name = "ACTIVE")
    private boolean active = true;

    @Column(name = "COLUMN_TO_UPDATE")
    private String columnToUpdate;

    @Type(type="hstore")
    @Column(name = "QUERY_ATTRIBUTES", columnDefinition = "hstore")
    private Map<String,String> queryAttributes = new HashMap<>();
}
