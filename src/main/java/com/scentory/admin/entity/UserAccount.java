package com.scentory.admin.entity;

import com.scentory.admin.model.RoleType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_accounts")
public class UserAccount extends BaseEntity {

    private String username;
    private String password;
    private String displayName;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    private boolean enabled = true;
}
