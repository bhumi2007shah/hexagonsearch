/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.responsebean.export;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author : sameer
 * Date : 10/06/20
 * Time : 8:31 PM
 * Class Name : JcmExportQAResponseBean
 * Project Name : server
 */
@Data
@Entity
@Table(name = "export_data_qa_view")
public class JcmExportQAResponseBean {
    @Id
    private Long jsqId;

    private Long jcmId;

    private String screeningQuestion;
    private String candidateResponse;
}
