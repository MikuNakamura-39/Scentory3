package com.scentory.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "workshop_menus")
public class WorkshopMenu extends BaseEntity {

    @NotBlank(message = "コース名は必須です。")
    private String name;

    @NotNull(message = "価格は必須です。")
    @DecimalMin(value = "0.0", inclusive = false, message = "価格は0より大きい値を入力してください。")
    private BigDecimal price;

    @NotNull(message = "所要時間は必須です。")
    @Min(value = 1, message = "所要時間は1分以上で入力してください。")
    private Integer durationMinutes;

    private boolean active = true;

    @Lob
    @Column(length = 4000)
    private String description;
}
