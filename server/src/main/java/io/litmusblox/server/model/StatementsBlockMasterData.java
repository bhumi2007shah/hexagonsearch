/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "STATEMENTS_BLOCK_MASTER_DATA")
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StatementsBlockMasterData {

    private static final long serialVersionUID = 6868521896546285047L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "STATEMENT_BLOCK")
    private String statementBlock;

    @Column(name = "QUESTION")
    private String question;

    @Column(name = "OPTIONS", columnDefinition = "varchar[]")
    @Type(type = "com.vladmihalcea.hibernate.type.array.StringArrayType")
    private String[]  options;
}
