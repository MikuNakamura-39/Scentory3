package com.scentory.admin.controller;

import com.scentory.admin.entity.Customer;
import com.scentory.admin.entity.Reservation;
import com.scentory.admin.entity.Staff;
import com.scentory.admin.entity.TimeSlot;
import com.scentory.admin.entity.WorkshopMenu;
import com.scentory.admin.model.ReservationStatus;
import com.scentory.admin.repository.CustomerRepository;
import com.scentory.admin.repository.ReservationRepository;
import com.scentory.admin.repository.StaffRepository;
import com.scentory.admin.repository.TimeSlotRepository;
import com.scentory.admin.repository.WorkshopMenuRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/public/reservations")
@RequiredArgsConstructor
public class PublicReservationController {

    private static final DateTimeFormatter RESERVATION_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;
    private final StaffRepository staffRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final WorkshopMenuRepository workshopMenuRepository;

    @PostMapping
    public String create(
        @ModelAttribute PublicReservationForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (form.getFullName() == null || form.getFullName().isBlank()) {
            bindingResult.reject("fullName", "氏名は必須です。");
        }
        if (form.getEmail() == null || form.getEmail().isBlank()) {
            bindingResult.reject("email", "メールアドレスは必須です。");
        }
        if (form.getPhone() == null || form.getPhone().isBlank()) {
            bindingResult.reject("phone", "電話番号は必須です。");
        }
        if (form.getReservationDate() == null || form.getReservationDate().isBlank()) {
            bindingResult.reject("reservationDate", "予約日を選択してください。");
        }
        if (form.getReservationTime() == null || form.getReservationTime().isBlank()) {
            bindingResult.reject("reservationTime", "予約時間を選択してください。");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("reservationError", "入力内容を確認してください。");
            return "redirect:/public/reservations/complete?error";
        }

        WorkshopMenu menu = workshopMenuRepository.findFirstByNameIgnoreCase(form.getMenuName())
            .or(() -> workshopMenuRepository.findFirstByActiveTrueOrderByIdAsc())
            .orElseThrow();

        Staff staff = staffRepository.findAll().stream()
            .filter(Staff::isActive)
            .findFirst()
            .orElseGet(() -> staffRepository.findAll().stream().findFirst().orElseThrow());

        LocalDate reservationDate = LocalDate.parse(form.getReservationDate());
        LocalTime reservationTime = LocalTime.parse(form.getReservationTime());
        LocalDateTime startAt = LocalDateTime.of(reservationDate, reservationTime);

        TimeSlot slot = timeSlotRepository.findFirstByStartAtAndWorkshopMenuIdOrderByIdAsc(startAt, menu.getId())
            .orElseGet(() -> {
                TimeSlot created = new TimeSlot();
                created.setStartAt(startAt);
                created.setEndAt(startAt.plusMinutes(menu.getDurationMinutes()));
                created.setCapacity(5);
                created.setBookedCount(0);
                created.setAccepting(true);
                created.setSlotType("LP予約");
                created.setStaff(staff);
                created.setWorkshopMenu(menu);
                return timeSlotRepository.save(created);
            });

        Customer customer = customerRepository.findFirstByEmailIgnoreCase(form.getEmail())
            .orElseGet(Customer::new);
        customer.setFullName(form.getFullName());
        customer.setEmail(form.getEmail());
        customer.setPhone(form.getPhone());
        customer.setChannel(form.getChannel());
        customer.setVisitCount(customer.getVisitCount() == null ? 0 : customer.getVisitCount());
        customer.setMemo(form.getNote());
        customer = customerRepository.save(customer);

        Reservation reservation = new Reservation();
        reservation.setReservationNumber("LP-" + startAt.format(RESERVATION_NUMBER_FORMAT) + "-" + (reservationRepository.count() + 1));
        reservation.setStatus(ReservationStatus.REQUESTED);
        reservation.setPartySize(form.getPartySize());
        reservation.setChannel(form.getChannel());
        reservation.setCustomer(customer);
        reservation.setTimeSlot(slot);
        reservation.setWorkshopMenu(menu);
        reservation.setAssignedStaff(slot.getStaff());
        reservationRepository.save(reservation);

        slot.setBookedCount(slot.getBookedCount() + form.getPartySize());
        timeSlotRepository.save(slot);

        redirectAttributes.addFlashAttribute("reservationNumber", reservation.getReservationNumber());
        redirectAttributes.addFlashAttribute("customerName", reservation.getCustomer().getFullName());
        redirectAttributes.addFlashAttribute("reservationDateTime", startAt);
        return "redirect:/public/reservations/complete";
    }

    @GetMapping("/complete")
    public String complete(Model model) {
        model.addAttribute("pageTitle", "予約受付完了");
        return "public-reservation-complete";
    }

    @Data
    public static class PublicReservationForm {
        @NotBlank
        private String fullName;
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String phone;
        @Min(1)
        @Max(10)
        private Integer partySize;
        private String note;
        @NotBlank
        private String reservationDate;
        @NotBlank
        private String reservationTime;
        private String channel = "LP Reservation";
        private String menuName = "オーダーフレグランス体験";
    }
}
