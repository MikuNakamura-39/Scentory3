package com.scentory.admin.service;

import com.scentory.admin.entity.Notice;
import com.scentory.admin.entity.Reservation;
import com.scentory.admin.entity.TimeSlot;
import com.scentory.admin.model.ReservationStatus;
import com.scentory.admin.repository.NoticeRepository;
import com.scentory.admin.repository.ReservationRepository;
import com.scentory.admin.repository.TimeSlotRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final NoticeRepository noticeRepository;

    public List<Reservation> todayReservations() {
        LocalDate today = LocalDate.now();
        return reservationRepository.findAll().stream()
            .filter(r -> r.getTimeSlot() != null)
            .filter(r -> r.getTimeSlot().getStartAt().toLocalDate().isEqual(today))
            .sorted(Comparator.comparing(r -> r.getTimeSlot().getStartAt()))
            .toList();
    }

    public List<TimeSlot> availableSlots() {
        return timeSlotRepository.findByStartAtBetweenOrderByStartAtAsc(LocalDateTime.now(), LocalDateTime.now().plusDays(7)).stream()
            .filter(TimeSlot::isAccepting)
            .limit(5)
            .toList();
    }

    public long reservationCount() {
        YearMonth currentMonth = YearMonth.now();
        return reservationRepository.findAll().stream()
            .filter(r -> r.getTimeSlot() != null && r.getTimeSlot().getStartAt() != null)
            .filter(r -> YearMonth.from(r.getTimeSlot().getStartAt().toLocalDate()).equals(currentMonth))
            .count();
    }

    public BigDecimal cancellationRate() {
        YearMonth currentMonth = YearMonth.now();
        List<Reservation> currentMonthReservations = reservationRepository.findAll().stream()
            .filter(r -> r.getTimeSlot() != null && r.getTimeSlot().getStartAt() != null)
            .filter(r -> YearMonth.from(r.getTimeSlot().getStartAt().toLocalDate()).equals(currentMonth))
            .toList();

        long total = currentMonthReservations.size();
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        long cancelled = currentMonthReservations.stream()
            .filter(r -> r.getStatus() == ReservationStatus.CANCELLED)
            .count();
        return BigDecimal.valueOf(cancelled * 100.0 / total).setScale(1, RoundingMode.HALF_UP);
    }

    public BigDecimal revenue() {
        YearMonth currentMonth = YearMonth.now();
        return reservationRepository.findAll().stream()
            .filter(r -> r.getTimeSlot() != null && r.getTimeSlot().getStartAt() != null)
            .filter(r -> YearMonth.from(r.getTimeSlot().getStartAt().toLocalDate()).equals(currentMonth))
            .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
            .map(r -> r.getWorkshopMenu() == null ? BigDecimal.ZERO : r.getWorkshopMenu().getPrice())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Notice> notices() {
        return noticeRepository.findAll().stream()
            .sorted(Comparator.comparing(Notice::isPinned).reversed())
            .limit(5)
            .toList();
    }
}
