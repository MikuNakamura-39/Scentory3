package com.scentory.admin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "time_slots")
public class TimeSlot extends BaseEntity {

    @NotNull(message = "開始日時は必須です。")
    private LocalDateTime startAt;

    @NotNull(message = "終了日時は必須です。")
    private LocalDateTime endAt;

    @NotNull(message = "定員は必須です。")
    @Min(value = 1, message = "定員は1以上で入力してください。")
    private Integer capacity;

    @NotNull(message = "予約済数は必須です。")
    @Min(value = 0, message = "予約済数は0以上で入力してください。")
    private Integer bookedCount = 0;

    private boolean accepting = true;
    private String slotType;

    @NotNull(message = "担当スタッフを選択してください。")
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @NotNull(message = "メニューを選択してください。")
    @ManyToOne
    @JoinColumn(name = "menu_id")
    private WorkshopMenu workshopMenu;
}
