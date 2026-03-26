package com.scentory.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "faq_entries")
public class FaqEntry extends BaseEntity {

    private String question;

    @Lob
    @Column(length = 4000)
    private String answer;

    private boolean active = true;
}
