package com.scentory.admin.entity;

import com.scentory.admin.model.RoleType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "staffs")
public class Staff extends BaseEntity {

    @NotBlank(message = "氏名は必須です。")
    private String name;

    @NotBlank(message = "メールアドレスは必須です。")
    @Email(message = "メールアドレスの形式で入力してください。")
    private String email;

    @NotBlank(message = "電話番号は必須です。")
    private String phone;

    @NotNull(message = "権限を選択してください。")
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    private boolean active = true;
}
