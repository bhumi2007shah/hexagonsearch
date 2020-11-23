/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : Shital Raval
 * Date : 18/10/19
 * Time : 11:19 AM
 * Class Name : CvRating
 * Project Name : server
 */
@Data
@Entity
@Table(name="CV_RATING")
@NoArgsConstructor
@JsonFilter("CvRating")
public class CvRating {
    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "JOB_CANDIDATE_MAPPING_ID")
    private Long jobCandidateMappingId;

    @Column(name = "OVERALL_RATING")
    private int overallRating;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "cvRatingId", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<CvRatingSkillKeywordDetails> cvRatingSkillKeywordDetails = new ArrayList<>();

    public CvRating(Long jobCandidateMappingId, int overallRating) {
        this.jobCandidateMappingId = jobCandidateMappingId;
        this.overallRating = overallRating;
    }
}
