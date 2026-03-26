package com.scentory.admin.model;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    REQUESTED("受付"),
    CONFIRMED("確定"),
    CHECKED_IN("来店済"),
    COMPLETED("完了"),
    CANCELLED("キャンセル");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }
}
