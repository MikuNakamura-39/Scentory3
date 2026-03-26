package com.scentory.admin.repository;

import com.scentory.admin.entity.TimeSlot;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByStartAtBetweenOrderByStartAtAsc(LocalDateTime from, LocalDateTime to);
    Optional<TimeSlot> findFirstByStartAtAndWorkshopMenuIdOrderByIdAsc(LocalDateTime startAt, Long workshopMenuId);
}
