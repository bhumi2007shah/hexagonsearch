/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * @author : Sumit
 * Date : 30/08/19
 * Time : 12:28 AM
 * Class Name : CvParsingDetails
 * Project Name : server
 */
@Data
@Entity
@Table(name = "CV_PARSING_DETAILS")
@NoArgsConstructor
public class CvParsingDetails {

    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CV_FILE_NAME")
    private String cvFileName;

    @Column(name = "PROCESSED_ON")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processedOn;

    @Column(name="PROCESSING_TIME")
    private Long processingTime;

    @Column(name="PROCESSING_STATUS")
    private String processingStatus;

    @Column(name = "PARSING_RESPONSE_JSON")
    private String parsingResponseJson;

    @Column(name = "PARSING_RESPONSE_TEXT")
    private String parsingResponseText;

    @Column(name = "PARSING_RESPONSE_HTML")
    private String parsingResponseHtml;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "CANDIDATE_ID")
    private Long candidateId;

    @Column(name = "RCHILLI_JSON_PROCESSED")
    private Boolean rchilliJsonProcessed;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "JOB_CANDIDATE_MAPPING_ID")
    private JobCandidateMapping jobCandidateMappingId;

    @Column(name = "CV_RATING_API_FLAG")
    private boolean cvRatingApiFlag = false;

    @Column(name="CV_RATING_API_RESPONSE_TIME")
    private Long cvRatingApiResponseTime;

    @Column(name = "CV_CONVERT_API_FLAG")
    private boolean cvConvertApiFlag = false;

    public CvParsingDetails(Date processedOn, String parsingResponseText, Long candidateId, JobCandidateMapping jobCandidateMappingId) {
        this.processedOn = processedOn;
        this.parsingResponseText = parsingResponseText;
        this.candidateId = candidateId;
        this.jobCandidateMappingId = jobCandidateMappingId;
    }
}
