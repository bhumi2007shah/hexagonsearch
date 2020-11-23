/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author : shital
 * Date : 18/10/19
 * Time : 11:22 AM
 * Class Name : CvRatingSkillKeywordDetails
 * Project Name : server
 */
@Data
@Entity
@Table(name = "CV_RATING_SKILL_KEYWORD_DETAILS")
@NoArgsConstructor
public class CvRatingSkillKeywordDetails {
    private static final long serialVersionUID = 6868521896546285046L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CV_RATING_ID")
    private Long cvRatingId;

    @Column(name = "SUPPORTING_KEYWORDS")
    private String supportingKeywords;

    @Column(name = "SKILL_NAME")
    private String skillName;

    @Column(name = "RATING")
    private int rating;

    @Column(name = "OCCURRENCE")
    private int occurrence;

    public CvRatingSkillKeywordDetails(Long cvRatingId, String supportingKeywords, String skillName, int rating, int occurrence) {
        this.cvRatingId = cvRatingId;
        this.supportingKeywords = supportingKeywords;
        this.skillName = skillName;
        this.rating = rating;
        this.occurrence = occurrence;
    }
}
