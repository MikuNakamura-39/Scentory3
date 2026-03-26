package com.scentory.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notices")
public class Notice extends BaseEntity {

    private String title;

    @Lob
    @Column(length = 4000)
    private String body;

    private boolean pinned;
    private LocalDateTime publishedAt;
}
