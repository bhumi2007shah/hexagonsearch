/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

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

    @Column(name = "PARSING_RESPONSE_TEXT")
    private String parsingResponseText;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "CANDIDATE_ID")
    private Long candidateId;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "JOB_CANDIDATE_MAPPING_ID")
    private JobCandidateMapping jobCandidateMappingId;

    @Column(name = "CV_RATING_API_FLAG")
    private boolean cvRatingApiFlag = false;

    @Column(name="CV_RATING_API_RESPONSE_TIME")
    private Long cvRatingApiResponseTime;

    @Column(name = "PARSING_RESPONSE_PYTHON")
    private String parsingResponsePython;

    @Column(name = "CV_RATING_API_CALL_RETRY_COUNT")
    private Long cvRatingApiCallTRetryCount = 0l;

    public CvParsingDetails(String cvFileName, Date processedOn, String parsingResponseText, Long candidateId, JobCandidateMapping jobCandidateMappingId) {
        this.cvFileName = cvFileName;
        this.processedOn = processedOn;
        this.parsingResponseText = parsingResponseText;
        this.candidateId = candidateId;
        this.jobCandidateMappingId = jobCandidateMappingId;
    }
}
