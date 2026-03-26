package com.scentory.admin.entity;

import com.scentory.admin.model.ReservationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
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
@Table(name = "reservations")
public class Reservation extends BaseEntity {

    @NotBlank(message = "予約番号は必須です。")
    private String reservationNumber;

    @NotNull(message = "ステータスを選択してください。")
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @NotNull(message = "人数を入力してください。")
    @Min(value = 1, message = "人数は1名以上で入力してください。")
    @Max(value = 10, message = "人数は10名以下で入力してください。")
    private Integer partySize;

    @NotBlank(message = "チャネルを入力してください。")
    private String channel;

    private BigDecimal refundAmount;
    private String cancellationNote;

    @NotNull(message = "顧客を選択してください。")
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @NotNull(message = "予約枠を選択してください。")
    @ManyToOne
    @JoinColumn(name = "slot_id")
    private TimeSlot timeSlot;

    @NotNull(message = "メニューを選択してください。")
    @ManyToOne
    @JoinColumn(name = "menu_id")
    private WorkshopMenu workshopMenu;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff assignedStaff;
}
