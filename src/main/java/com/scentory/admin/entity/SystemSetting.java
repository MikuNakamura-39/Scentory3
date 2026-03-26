package com.scentory.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "system_settings")
public class SystemSetting extends BaseEntity {

    @Column(unique = true)
    private String settingKey;

    @Column(length = 4000)
    private String settingValue;
}
