package com.scentory.admin.controller;

import com.scentory.admin.entity.Reservation;
import com.scentory.admin.entity.Customer;
import com.scentory.admin.entity.Staff;
import com.scentory.admin.entity.TimeSlot;
import com.scentory.admin.model.ReservationStatus;
import com.scentory.admin.repository.CustomerRepository;
import com.scentory.admin.repository.ReservationRepository;
import com.scentory.admin.repository.StaffRepository;
import com.scentory.admin.repository.TimeSlotRepository;
import com.scentory.admin.repository.WorkshopMenuRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private static final List<String> CHANNEL_OPTIONS = List.of("Web", "Instagram", "TEL", "LINE", "店頭");
    private static final List<String> TIME_OPTIONS = java.util.stream.IntStream.rangeClosed(22, 35)
        .mapToObj(index -> LocalTime.of(index / 2, (index % 2) * 30).format(DateTimeFormatter.ofPattern("HH:mm")))
        .toList();

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final WorkshopMenuRepository workshopMenuRepository;
    private final StaffRepository staffRepository;
    private final Validator validator;

    @GetMapping
    public String list(
        @RequestParam(required = false) ReservationStatus status,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String channel,
        @RequestParam(required = false) Integer partySize,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reservationDate,
        @RequestParam(required = false) String calendarMonth,
        @RequestParam(defaultValue = "1") int page,
        Model model
    ) {
        final int pageSize = 10;
        List<Reservation> baseReservations = reservationRepository.findAll().stream()
            .filter(item -> status == null || item.getStatus() == status)
            .filter(item -> keyword == null || keyword.isBlank()
                || (item.getCustomer() != null && item.getCustomer().getFullName() != null
                && item.getCustomer().getFullName().toLowerCase().contains(keyword.toLowerCase())))
            .filter(item -> channel == null || channel.isBlank()
                || (item.getChannel() != null && item.getChannel().toLowerCase().contains(channel.toLowerCase())))
            .filter(item -> partySize == null || (item.getPartySize() != null && item.getPartySize().equals(partySize)))
            .sorted(Comparator.comparing(
                item -> item.getTimeSlot() != null ? item.getTimeSlot().getStartAt() : null,
                Comparator.nullsLast(Comparator.naturalOrder())
            ))
            .toList();

        List<Reservation> filteredReservations = baseReservations.stream()
            .filter(item -> reservationDate == null
                || (item.getTimeSlot() != null && item.getTimeSlot().getStartAt() != null
                && item.getTimeSlot().getStartAt().toLocalDate().isEqual(reservationDate)))
            .toList();

        YearMonth displayedMonth = resolveDisplayedMonth(calendarMonth, reservationDate);
        Map<LocalDate, List<ReservationCalendarItem>> reservationsByDate = baseReservations.stream()
            .filter(item -> item.getTimeSlot() != null && item.getTimeSlot().getStartAt() != null)
            .filter(item -> YearMonth.from(item.getTimeSlot().getStartAt().toLocalDate()).equals(displayedMonth))
            .collect(java.util.stream.Collectors.groupingBy(
                item -> item.getTimeSlot().getStartAt().toLocalDate(),
                java.util.stream.Collectors.mapping(ReservationCalendarItem::from, java.util.stream.Collectors.toList())
            ));

        List<ReservationCalendarCell> calendarCells = buildCalendarCells(displayedMonth, reservationsByDate);

        int totalPages = Math.max(1, (int) Math.ceil((double) filteredReservations.size() / pageSize));
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int fromIndex = Math.min((currentPage - 1) * pageSize, filteredReservations.size());
        int toIndex = Math.min(fromIndex + pageSize, filteredReservations.size());

        model.addAttribute("pageTitle", "予約管理");
        model.addAttribute("statuses", ReservationStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("channel", channel);
        model.addAttribute("partySize", partySize);
        model.addAttribute("reservationDate", reservationDate);
        model.addAttribute("calendarMonth", displayedMonth.toString());
        model.addAttribute("calendarLabel", displayedMonth.getYear() + "年" + displayedMonth.getMonthValue() + "月");
        model.addAttribute("previousCalendarMonth", displayedMonth.minusMonths(1).toString());
        model.addAttribute("nextCalendarMonth", displayedMonth.plusMonths(1).toString());
        model.addAttribute("calendarCells", calendarCells);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrevious", currentPage > 1);
        model.addAttribute("hasNext", currentPage < totalPages);
        model.addAttribute("reservations", filteredReservations.subList(fromIndex, toIndex));
        return "reservations/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "予約詳細");
        model.addAttribute("reservation", reservationRepository.findById(id).orElseThrow());
        return "reservations/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.REQUESTED);
        reservation.setReservationNumber(generateReservationNumber());
        prepareForm(model, reservation, "予約登録");
        return "reservations/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        prepareForm(model, reservationRepository.findById(id).orElseThrow(), "予約編集");
        return "reservations/form";
    }

    @PostMapping("/save")
    public String save(
        @ModelAttribute Reservation reservation,
        BindingResult bindingResult,
        @RequestParam String customerName,
        @RequestParam(required = false) String reservationDateOnly,
        @RequestParam(required = false) String reservationTimeOnly,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Customer customer = resolveCustomer(customerName, reservation);
        if (customer == null) {
            bindingResult.rejectValue("customer", "customer.required", "顧客名を入力してください。");
        } else {
            reservation.setCustomer(customer);
        }

        if (reservation.getPartySize() == null) {
            bindingResult.rejectValue("partySize", "partySize.required", "人数を選択してください。");
        }

        if (reservation.getWorkshopMenu() == null) {
            bindingResult.rejectValue("workshopMenu", "workshopMenu.required", "メニューを選択してください。");
        }

        if (!bindingResult.hasFieldErrors("timeSlot")) {
            try {
                String reservationStartAt = combineReservationStartAt(reservationDateOnly, reservationTimeOnly);
                TimeSlot resolvedSlot = resolveTimeSlot(reservationStartAt, reservation);
                reservation.setTimeSlot(resolvedSlot);
            } catch (IllegalArgumentException error) {
                bindingResult.rejectValue("timeSlot", "timeSlot.invalid", error.getMessage());
            }
        }

        Set<ConstraintViolation<Reservation>> violations = validator.validate(reservation);
        for (ConstraintViolation<Reservation> violation : violations) {
            String field = violation.getPropertyPath().toString();
            if (!bindingResult.hasFieldErrors(field)) {
                bindingResult.rejectValue(field, field + ".invalid", violation.getMessage());
            }
        }

        if (bindingResult.hasErrors()) {
            prepareForm(model, reservation, reservation.getId() == null ? "予約登録" : "予約編集");
            model.addAttribute("customerName", customerName);
            model.addAttribute("reservationDateOnly", reservationDateOnly);
            model.addAttribute("reservationTimeOnly", reservationTimeOnly);
            return "reservations/form";
        }
        if (reservation.getReservationNumber() == null || reservation.getReservationNumber().isBlank()) {
            reservation.setReservationNumber(generateReservationNumber());
        }
        reservationRepository.save(reservation);
        redirectAttributes.addFlashAttribute("message", "予約を保存しました。");
        return "redirect:/reservations";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(
        @PathVariable Long id,
        @RequestParam(required = false) BigDecimal refundAmount,
        @RequestParam(required = false) String cancellationNote,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (cancellationNote == null || cancellationNote.isBlank()) {
            model.addAttribute("pageTitle", "予約詳細");
            model.addAttribute("reservation", reservationRepository.findById(id).orElseThrow());
            model.addAttribute("errorMessage", "キャンセル理由を入力してください。");
            return "reservations/detail";
        }
        Reservation reservation = reservationRepository.findById(id).orElseThrow();
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setRefundAmount(refundAmount);
        reservation.setCancellationNote(cancellationNote);
        reservationRepository.save(reservation);
        redirectAttributes.addFlashAttribute("message", "予約をキャンセルしました。");
        return "redirect:/reservations/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
        @PathVariable Long id,
        @RequestParam ReservationStatus status,
        @RequestParam(required = false) String redirectTo,
        @RequestParam(required = false) Integer redirectPage,
        @RequestParam(required = false) ReservationStatus redirectStatus,
        @RequestParam(required = false) String redirectKeyword,
        @RequestParam(required = false) String redirectChannel,
        @RequestParam(required = false) Integer redirectPartySize,
        @RequestParam(required = false) String redirectReservationDate,
        @RequestParam(required = false) String redirectCalendarMonth,
        RedirectAttributes redirectAttributes
    ) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow();
        reservation.setStatus(status);
        reservationRepository.save(reservation);
        redirectAttributes.addFlashAttribute("message", "予約ステータスを更新しました。");
        if ("list".equalsIgnoreCase(redirectTo)) {
            if (redirectPage != null) {
                redirectAttributes.addAttribute("page", redirectPage);
            }
            if (redirectStatus != null) {
                redirectAttributes.addAttribute("status", redirectStatus);
            }
            if (redirectKeyword != null && !redirectKeyword.isBlank()) {
                redirectAttributes.addAttribute("keyword", redirectKeyword);
            }
            if (redirectChannel != null && !redirectChannel.isBlank()) {
                redirectAttributes.addAttribute("channel", redirectChannel);
            }
            if (redirectPartySize != null) {
                redirectAttributes.addAttribute("partySize", redirectPartySize);
            }
            if (redirectReservationDate != null && !redirectReservationDate.isBlank()) {
                redirectAttributes.addAttribute("reservationDate", redirectReservationDate);
            }
            if (redirectCalendarMonth != null && !redirectCalendarMonth.isBlank()) {
                redirectAttributes.addAttribute("calendarMonth", redirectCalendarMonth);
            }
            return "redirect:/reservations";
        }
        return "redirect:/reservations/" + id;
    }

    private void prepareForm(Model model, Reservation reservation, String title) {
        List<com.scentory.admin.entity.WorkshopMenu> menus = workshopMenuRepository.findAll();
        if (reservation.getReservationNumber() == null || reservation.getReservationNumber().isBlank()) {
            reservation.setReservationNumber(generateReservationNumber());
        }
        if (reservation.getWorkshopMenu() == null) {
            menus.stream()
                .filter(menu -> "オーダーフレグランス体験".equals(menu.getName()))
                .findFirst()
                .ifPresentOrElse(
                    reservation::setWorkshopMenu,
                    () -> {
                        if (menus.size() == 1) {
                            reservation.setWorkshopMenu(menus.get(0));
                        }
                    }
                );
        }
        model.addAttribute("pageTitle", title);
        model.addAttribute("reservation", reservation);
        model.addAttribute("reservationNumberDisplay", reservation.getReservationNumber());
        model.addAttribute("customerName",
            reservation.getCustomer() != null && reservation.getCustomer().getFullName() != null
                ? reservation.getCustomer().getFullName()
                : "");
        model.addAttribute("reservationDateOnly",
            reservation.getTimeSlot() != null && reservation.getTimeSlot().getStartAt() != null
                ? reservation.getTimeSlot().getStartAt().toLocalDate().toString()
                : "");
        model.addAttribute("reservationTimeOnly",
            reservation.getTimeSlot() != null && reservation.getTimeSlot().getStartAt() != null
                ? reservation.getTimeSlot().getStartAt().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "");
        model.addAttribute("statuses", ReservationStatus.values());
        model.addAttribute("slots", timeSlotRepository.findAll());
        model.addAttribute("menus", menus);
        model.addAttribute("staffs", staffRepository.findAll());
        model.addAttribute("channels", CHANNEL_OPTIONS);
        model.addAttribute("timeOptions", TIME_OPTIONS);
    }

    private Customer resolveCustomer(String customerName, Reservation reservation) {
        if (customerName == null || customerName.isBlank()) {
            return null;
        }
        String normalizedName = customerName.trim();
        if (reservation.getCustomer() != null
            && reservation.getCustomer().getFullName() != null
            && reservation.getCustomer().getFullName().equalsIgnoreCase(normalizedName)) {
            return reservation.getCustomer();
        }

        return customerRepository.findFirstByFullNameIgnoreCase(normalizedName)
            .orElseGet(() -> {
                Customer customer = new Customer();
                customer.setFullName(normalizedName);
                customer.setEmail("admin-" + System.currentTimeMillis() + "@scentory.local");
                customer.setPhone("000-0000-0000");
                customer.setChannel("管理画面");
                customer.setVisitCount(0);
                customer.setMemo("予約管理画面から作成");
                return customerRepository.save(customer);
            });
    }

    private TimeSlot resolveTimeSlot(String reservationStartAt, Reservation reservation) {
        if (reservationStartAt == null || reservationStartAt.isBlank()) {
            throw new IllegalArgumentException("予約日時を入力してください。");
        }
        if (reservation.getWorkshopMenu() == null) {
            throw new IllegalArgumentException("先にメニューを選択してください。");
        }

        final LocalDateTime startAt;
        try {
            startAt = LocalDateTime.parse(reservationStartAt);
        } catch (DateTimeParseException error) {
            throw new IllegalArgumentException("予約日時の形式が正しくありません。");
        }

        return timeSlotRepository.findFirstByStartAtAndWorkshopMenuIdOrderByIdAsc(startAt, reservation.getWorkshopMenu().getId())
            .orElseGet(() -> {
                Staff slotStaff = reservation.getAssignedStaff();
                if (slotStaff == null && reservation.getTimeSlot() != null) {
                    slotStaff = reservation.getTimeSlot().getStaff();
                }
                if (slotStaff == null) {
                    slotStaff = staffRepository.findAll().stream()
                        .filter(Staff::isActive)
                        .findFirst()
                        .orElseGet(() -> staffRepository.findAll().stream().findFirst().orElseThrow());
                }

                TimeSlot slot = new TimeSlot();
                slot.setStartAt(startAt);
                slot.setEndAt(startAt.plusMinutes(reservation.getWorkshopMenu().getDurationMinutes()));
                slot.setCapacity(10);
                slot.setBookedCount(0);
                slot.setAccepting(true);
                slot.setSlotType("管理画面登録");
                slot.setStaff(slotStaff);
                slot.setWorkshopMenu(reservation.getWorkshopMenu());
                return timeSlotRepository.save(slot);
            });
    }

    private YearMonth resolveDisplayedMonth(String calendarMonth, LocalDate reservationDate) {
        if (calendarMonth != null && !calendarMonth.isBlank()) {
            try {
                return YearMonth.parse(calendarMonth);
            } catch (DateTimeParseException ignored) {
            }
        }
        if (reservationDate != null) {
            return YearMonth.from(reservationDate);
        }
        return YearMonth.now();
    }

    private String combineReservationStartAt(String reservationDateOnly, String reservationTimeOnly) {
        if (reservationDateOnly == null || reservationDateOnly.isBlank()) {
            throw new IllegalArgumentException("予約日を入力してください。");
        }
        if (reservationTimeOnly == null || reservationTimeOnly.isBlank()) {
            throw new IllegalArgumentException("予約時間を選択してください。");
        }
        return reservationDateOnly + "T" + reservationTimeOnly;
    }

    private String generateReservationNumber() {
        return "RSV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private List<ReservationCalendarCell> buildCalendarCells(
        YearMonth displayedMonth,
        Map<LocalDate, List<ReservationCalendarItem>> reservationsByDate
    ) {
        List<ReservationCalendarCell> cells = new ArrayList<>();
        LocalDate firstDay = displayedMonth.atDay(1);
        int leadingEmptyCells = firstDay.getDayOfWeek().getValue() % 7;
        for (int index = 0; index < leadingEmptyCells; index += 1) {
            cells.add(ReservationCalendarCell.empty());
        }

        for (int day = 1; day <= displayedMonth.lengthOfMonth(); day += 1) {
            LocalDate date = displayedMonth.atDay(day);
            cells.add(ReservationCalendarCell.of(date, reservationsByDate.getOrDefault(date, List.of())));
        }

        while (cells.size() % 7 != 0) {
            cells.add(ReservationCalendarCell.empty());
        }
        return cells;
    }

    public static class ReservationCalendarCell {
        private final String dayLabel;
        private final boolean blank;
        private final List<ReservationCalendarItem> reservations;

        private ReservationCalendarCell(String dayLabel, boolean blank, List<ReservationCalendarItem> reservations) {
            this.dayLabel = dayLabel;
            this.blank = blank;
            this.reservations = reservations;
        }

        public static ReservationCalendarCell empty() {
            return new ReservationCalendarCell("", true, List.of());
        }

        public static ReservationCalendarCell of(LocalDate date, List<ReservationCalendarItem> reservations) {
            return new ReservationCalendarCell(String.valueOf(date.getDayOfMonth()), false, reservations);
        }

        public String getDayLabel() {
            return dayLabel;
        }

        public List<ReservationCalendarItem> getReservations() {
            return reservations;
        }

        public boolean isBlank() {
            return blank;
        }
    }

    public static class ReservationCalendarItem {
        private final Long id;
        private final String timeLabel;
        private final String customerName;
        private final String statusLabel;
        private final String statusName;

        private ReservationCalendarItem(Long id, String timeLabel, String customerName, String statusLabel, String statusName) {
            this.id = id;
            this.timeLabel = timeLabel;
            this.customerName = customerName;
            this.statusLabel = statusLabel;
            this.statusName = statusName;
        }

        public static ReservationCalendarItem from(Reservation reservation) {
            return new ReservationCalendarItem(
                reservation.getId(),
                reservation.getTimeSlot().getStartAt().toLocalTime().toString(),
                reservation.getCustomer() != null ? reservation.getCustomer().getFullName() : "顧客未設定",
                reservation.getStatus().getLabel(),
                reservation.getStatus().name().toLowerCase()
            );
        }

        public Long getId() {
            return id;
        }

        public String getTimeLabel() {
            return timeLabel;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getStatusLabel() {
            return statusLabel;
        }

        public String getStatusName() {
            return statusName;
        }
    }
}
