package com.scentory.admin.controller;

import com.scentory.admin.entity.Reservation;
import com.scentory.admin.entity.Staff;
import com.scentory.admin.entity.TimeSlot;
import com.scentory.admin.model.ReservationStatus;
import com.scentory.admin.repository.ReservationRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;
import com.scentory.admin.repository.StaffRepository;
import com.scentory.admin.repository.TimeSlotRepository;
import com.scentory.admin.repository.WorkshopMenuRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/slots")
@RequiredArgsConstructor
public class ScheduleController {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Tokyo");

    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;
    private final StaffRepository staffRepository;
    private final WorkshopMenuRepository workshopMenuRepository;

    @GetMapping
    public String list(
        @RequestParam(required = false) String calendarMonth,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduleDate,
        @RequestParam(required = false) Long staffId,
        Model model
    ) {
        YearMonth displayedMonth = resolveDisplayedMonth(calendarMonth);
        List<Reservation> monthlyReservations = reservationRepository.findAll().stream()
            .filter(reservation -> reservation.getTimeSlot() != null && reservation.getTimeSlot().getStartAt() != null)
            .filter(reservation -> YearMonth.from(reservation.getTimeSlot().getStartAt().toLocalDate()).equals(displayedMonth))
            .sorted(Comparator.comparing(
                reservation -> reservation.getTimeSlot().getStartAt(),
                Comparator.nullsLast(Comparator.naturalOrder())
            ))
            .toList();

        List<TimeSlot> monthlySlots = timeSlotRepository.findAll().stream()
            .filter(slot -> slot.getStartAt() != null
                && YearMonth.from(slot.getStartAt().toLocalDate()).equals(displayedMonth))
            .sorted(Comparator.comparing(TimeSlot::getStartAt, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();

        LocalDate displayedDate = resolveDisplayedDate(scheduleDate, displayedMonth, monthlyReservations, monthlySlots);

        List<TimeSlot> slots = monthlySlots.stream()
            .filter(slot -> isOnDate(slot, displayedDate))
            .filter(slot -> staffId == null
                || (slot.getStaff() != null && slot.getStaff().getId().equals(staffId)))
            .toList();

        List<Reservation> reservations = monthlyReservations.stream()
            .filter(reservation -> isOnDate(reservation.getTimeSlot(), displayedDate))
            .filter(reservation -> staffId == null || hasStaff(reservation, staffId))
            .toList();

        List<Staff> allStaffs = staffRepository.findAll().stream()
            .sorted(Comparator.comparing(Staff::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();

        List<Staff> displayedStaffs = allStaffs.stream()
            .filter(staff -> staffId == null || staff.getId().equals(staffId))
            .toList();

        TimelineBounds timelineBounds = resolveTimelineBounds(slots, reservations, displayedDate);
        List<String> timelineHours = buildTimelineHours(timelineBounds.start(), timelineBounds.end());
        List<ScheduleRowView> scheduleRows = displayedStaffs.stream()
            .map(staff -> toScheduleRow(staff, reservations, displayedDate, timelineBounds))
            .toList();
        Map<LocalDate, Long> reservationCountsByDate = monthlyReservations.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                reservation -> reservation.getTimeSlot().getStartAt().toLocalDate(),
                java.util.stream.Collectors.counting()
            ));
        List<ScheduleCalendarCell> calendarCells = buildCalendarCells(
            displayedMonth,
            displayedDate,
            LocalDate.now(APP_ZONE),
            reservationCountsByDate
        );

        model.addAttribute("pageTitle", "スケジュール管理");
        model.addAttribute("calendarMonth", displayedMonth.toString());
        model.addAttribute("calendarLabel", displayedMonth.getYear() + "年" + displayedMonth.getMonthValue() + "月");
        model.addAttribute("previousCalendarMonth", displayedMonth.minusMonths(1).toString());
        model.addAttribute("nextCalendarMonth", displayedMonth.plusMonths(1).toString());
        model.addAttribute("scheduleDate", displayedDate);
        model.addAttribute("scheduleDateLabel",
            String.format(Locale.JAPAN, "%d年%d月%d日", displayedDate.getYear(), displayedDate.getMonthValue(), displayedDate.getDayOfMonth()));
        model.addAttribute("previousDate", displayedDate.minusDays(1));
        model.addAttribute("nextDate", displayedDate.plusDays(1));
        model.addAttribute("previousDateMonth", YearMonth.from(displayedDate.minusDays(1)).toString());
        model.addAttribute("nextDateMonth", YearMonth.from(displayedDate.plusDays(1)).toString());
        model.addAttribute("selectedStaffId", staffId);
        model.addAttribute("staffs", allStaffs);
        model.addAttribute("slotCount", slots.size());
        model.addAttribute("availableCount", slots.stream().filter(slot -> slot.isAccepting() && slot.getBookedCount() < slot.getCapacity()).count());
        model.addAttribute("reservationCount", reservations.size());
        model.addAttribute("timelineHours", timelineHours);
        model.addAttribute("timelineColumnCount", Math.max(1, timelineHours.size()));
        model.addAttribute("scheduleRows", scheduleRows);
        model.addAttribute("calendarCells", calendarCells);
        return "slots/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        TimeSlot slot = new TimeSlot();
        slot.setAccepting(true);
        prepareForm(model, slot, "枠編集");
        return "slots/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        prepareForm(model, timeSlotRepository.findById(id).orElseThrow(), "枠編集");
        return "slots/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute TimeSlot slot, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, slot, "枠編集");
            return "slots/form";
        }
        timeSlotRepository.save(slot);
        redirectAttributes.addFlashAttribute("message", "枠設定を保存しました。");
        return "redirect:/slots";
    }

    private void prepareForm(Model model, TimeSlot slot, String pageTitle) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("slot", slot);
        model.addAttribute("staffs", staffRepository.findAll());
        model.addAttribute("menus", workshopMenuRepository.findAll());
    }

    private YearMonth resolveDisplayedMonth(String calendarMonth) {
        if (calendarMonth != null && !calendarMonth.isBlank()) {
            try {
                return YearMonth.parse(calendarMonth);
            } catch (DateTimeParseException ignored) {
            }
        }
        return YearMonth.now(APP_ZONE);
    }

    private LocalDate resolveDisplayedDate(
        LocalDate scheduleDate,
        YearMonth displayedMonth,
        List<Reservation> monthlyReservations,
        List<TimeSlot> monthlySlots
    ) {
        if (scheduleDate != null) {
            return scheduleDate;
        }
        LocalDate today = LocalDate.now(APP_ZONE);
        if (YearMonth.from(today).equals(displayedMonth)) {
            return today;
        }
        if (!monthlyReservations.isEmpty()) {
            return monthlyReservations.get(0).getTimeSlot().getStartAt().toLocalDate();
        }
        if (!monthlySlots.isEmpty()) {
            return monthlySlots.get(0).getStartAt().toLocalDate();
        }
        return displayedMonth.atDay(1);
    }

    private boolean isOnDate(TimeSlot slot, LocalDate date) {
        return slot.getStartAt() != null && slot.getStartAt().toLocalDate().equals(date);
    }

    private boolean hasStaff(Reservation reservation, Long staffId) {
        Staff reservationStaff = resolveStaff(reservation);
        return reservationStaff != null && reservationStaff.getId().equals(staffId);
    }

    private Staff resolveStaff(Reservation reservation) {
        if (reservation.getAssignedStaff() != null) {
            return reservation.getAssignedStaff();
        }
        if (reservation.getTimeSlot() != null) {
            return reservation.getTimeSlot().getStaff();
        }
        return null;
    }

    private TimelineBounds resolveTimelineBounds(List<TimeSlot> slots, List<Reservation> reservations, LocalDate displayedDate) {
        return new TimelineBounds(LocalTime.of(11, 0), LocalTime.of(19, 0));
    }

    private List<String> buildTimelineHours(LocalTime start, LocalTime end) {
        int hourCount = (int) Duration.between(start, end).toHours();
        return IntStream.range(0, hourCount)
            .mapToObj(index -> String.format(Locale.JAPAN, "%02d:00", start.plusHours(index).getHour()))
            .toList();
    }

    private ScheduleRowView toScheduleRow(Staff staff, List<Reservation> reservations, LocalDate displayedDate, TimelineBounds bounds) {
        LocalDateTime timelineStart = displayedDate.atTime(bounds.start());
        LocalDateTime timelineEnd = displayedDate.atTime(bounds.end());
        long totalMinutes = Duration.between(timelineStart, timelineEnd).toMinutes();

        List<ScheduleEventView> events = reservations.stream()
            .filter(reservation -> {
                Staff reservationStaff = resolveStaff(reservation);
                return reservationStaff != null && reservationStaff.getId().equals(staff.getId());
            })
            .filter(reservation -> reservation.getTimeSlot() != null
                && reservation.getTimeSlot().getStartAt() != null
                && reservation.getTimeSlot().getEndAt() != null)
            .sorted(Comparator.comparing(reservation -> reservation.getTimeSlot().getStartAt()))
            .map(reservation -> toScheduleEvent(reservation, displayedDate, bounds, totalMinutes))
            .toList();

        return new ScheduleRowView(staff.getName(), events);
    }

    private ScheduleEventView toScheduleEvent(
        Reservation reservation,
        LocalDate displayedDate,
        TimelineBounds bounds,
        long totalMinutes
    ) {
        TimeSlot slot = reservation.getTimeSlot();
        LocalDateTime slotStart = slot.getStartAt();
        LocalDateTime slotEnd = slot.getEndAt();
        LocalDateTime timelineStart = displayedDate.atTime(bounds.start());
        LocalDateTime timelineEnd = displayedDate.atTime(bounds.end());

        LocalDateTime clippedStart = slotStart.isBefore(timelineStart) ? timelineStart : slotStart;
        LocalDateTime clippedEnd = slotEnd.isAfter(timelineEnd) ? timelineEnd : slotEnd;

        long startMinutes = Math.max(0, Duration.between(timelineStart, clippedStart).toMinutes());
        long durationMinutes = Math.max(30, Duration.between(clippedStart, clippedEnd).toMinutes());

        double leftPercent = totalMinutes == 0 ? 0 : (startMinutes * 100.0 / totalMinutes);
        double widthPercent = totalMinutes == 0 ? 0 : (durationMinutes * 100.0 / totalMinutes);

        String customerName = reservation.getCustomer() != null ? reservation.getCustomer().getFullName() + "様" : "顧客未設定";
        String timeLabel = String.format(Locale.JAPAN, "%02d:%02d - %02d:%02d",
            slotStart.getHour(), slotStart.getMinute(), slotEnd.getHour(), slotEnd.getMinute());
        String partyLabel = reservation.getPartySize() != null ? reservation.getPartySize() + "名" : "";
        ReservationStatus status = reservation.getStatus() != null ? reservation.getStatus() : ReservationStatus.CONFIRMED;
        String statusClass = "status-" + status.name().toLowerCase(Locale.ROOT);
        String statusLabel = status.getLabel();

        String leftPosition = String.format(Locale.ROOT, "%.2f", leftPercent);
        String widthSize = String.format(Locale.ROOT, "%.2f", widthPercent);

        return new ScheduleEventView(customerName, timeLabel, partyLabel, statusClass, statusLabel, leftPosition, widthSize);
    }

    private record TimelineBounds(LocalTime start, LocalTime end) {
    }

    public record ScheduleRowView(String staffName, List<ScheduleEventView> events) {
    }

    public record ScheduleEventView(
        String customerName,
        String timeLabel,
        String partyLabel,
        String statusClass,
        String statusLabel,
        String leftPosition,
        String widthSize
    ) {
    }

    private List<ScheduleCalendarCell> buildCalendarCells(
        YearMonth displayedMonth,
        LocalDate selectedDate,
        LocalDate today,
        Map<LocalDate, Long> reservationCountsByDate
    ) {
        List<ScheduleCalendarCell> cells = new ArrayList<>();
        LocalDate firstDay = displayedMonth.atDay(1);
        int leadingEmptyCells = firstDay.getDayOfWeek().getValue() % 7;
        for (int index = 0; index < leadingEmptyCells; index += 1) {
            cells.add(ScheduleCalendarCell.empty());
        }

        for (int day = 1; day <= displayedMonth.lengthOfMonth(); day += 1) {
            LocalDate date = displayedMonth.atDay(day);
            cells.add(ScheduleCalendarCell.of(
                date,
                date.equals(selectedDate),
                date.equals(today),
                reservationCountsByDate.getOrDefault(date, 0L)
            ));
        }

        while (cells.size() % 7 != 0) {
            cells.add(ScheduleCalendarCell.empty());
        }
        return cells;
    }

    public static class ScheduleCalendarCell {
        private final LocalDate date;
        private final String dayLabel;
        private final boolean blank;
        private final boolean selected;
        private final boolean today;
        private final long reservationCount;

        private ScheduleCalendarCell(LocalDate date, String dayLabel, boolean blank, boolean selected, boolean today, long reservationCount) {
            this.date = date;
            this.dayLabel = dayLabel;
            this.blank = blank;
            this.selected = selected;
            this.today = today;
            this.reservationCount = reservationCount;
        }

        public static ScheduleCalendarCell empty() {
            return new ScheduleCalendarCell(null, "", true, false, false, 0);
        }

        public static ScheduleCalendarCell of(LocalDate date, boolean selected, boolean today, long reservationCount) {
            return new ScheduleCalendarCell(date, String.valueOf(date.getDayOfMonth()), false, selected, today, reservationCount);
        }

        public LocalDate getDate() {
            return date;
        }

        public String getDayLabel() {
            return dayLabel;
        }

        public boolean isBlank() {
            return blank;
        }

        public boolean isSelected() {
            return selected;
        }

        public boolean isToday() {
            return today;
        }

        public boolean isHasReservation() {
            return reservationCount > 0;
        }

        public long getReservationCount() {
            return reservationCount;
        }
    }
}
