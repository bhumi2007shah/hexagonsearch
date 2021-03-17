/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vladmihalcea.hibernate.type.array.LongArrayType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Entity
@Table(name = "UNVERIFIED_SKILLS")
@TypeDef(
        name = "list-array",
        typeClass = LongArrayType.class
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor
public class UnverifiedSkills implements Serializable

{
    @Id
    @NotNull
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private   Long id;

    public UnverifiedSkills(String skill, Long[] CANDIDATE_ID) {
        this.id = id;
        this.skill = skill;
        this.candiateIds = CANDIDATE_ID;
    }

    @NotNull
    @Column(name="SKILLNAME")
    private  String skill;


    @Type(type = "list-array")
    @Column(name="CANDIDATE_IDS",columnDefinition = "Long[]")
    private Long[] candiateIds;
}
