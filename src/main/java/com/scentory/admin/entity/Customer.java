package com.scentory.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    @NotBlank(message = "氏名は必須です。")
    private String fullName;

    @NotBlank(message = "メールアドレスは必須です。")
    @Email(message = "メールアドレスの形式で入力してください。")
    private String email;

    @NotBlank(message = "電話番号は必須です。")
    private String phone;

    private String channel;
    private String tags;

    @NotNull(message = "来店回数を入力してください。")
    @PositiveOrZero(message = "来店回数は0以上で入力してください。")
    private Integer visitCount = 0;

    @Lob
    @Column(length = 4000)
    private String memo;
}
