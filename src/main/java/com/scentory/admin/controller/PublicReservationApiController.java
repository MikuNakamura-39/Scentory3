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
import jakarta.validation.Valid;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicReservationApiController {

    private static final DateTimeFormatter RESERVATION_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;
    private final StaffRepository staffRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final WorkshopMenuRepository workshopMenuRepository;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody PublicReservationRequest request) {
        WorkshopMenu menu = workshopMenuRepository.findFirstByNameIgnoreCase(request.getMenuName())
            .or(() -> workshopMenuRepository.findFirstByActiveTrueOrderByIdAsc())
            .orElseThrow();

        Staff staff = staffRepository.findAll().stream()
            .filter(Staff::isActive)
            .findFirst()
            .orElseGet(() -> staffRepository.findAll().stream().findFirst().orElseThrow());

        LocalDate reservationDate = LocalDate.parse(request.getReservationDate());
        LocalTime reservationTime = LocalTime.parse(request.getReservationTime());
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

        Customer customer = customerRepository.findFirstByEmailIgnoreCase(request.getEmail())
            .orElseGet(Customer::new);
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setChannel(request.getChannel());
        customer.setVisitCount(customer.getVisitCount() == null ? 0 : customer.getVisitCount());
        customer.setMemo(request.getNote());
        customer = customerRepository.save(customer);

        Reservation reservation = new Reservation();
        reservation.setReservationNumber("LP-" + startAt.format(RESERVATION_NUMBER_FORMAT) + "-" + (reservationRepository.count() + 1));
        reservation.setStatus(ReservationStatus.REQUESTED);
        reservation.setPartySize(request.getPartySize());
        reservation.setChannel(request.getChannel());
        reservation.setCustomer(customer);
        reservation.setTimeSlot(slot);
        reservation.setWorkshopMenu(menu);
        reservation.setAssignedStaff(slot.getStaff());
        reservationRepository.save(reservation);

        slot.setBookedCount(slot.getBookedCount() + request.getPartySize());
        timeSlotRepository.save(slot);

        ReservationResponse response = new ReservationResponse();
        response.setReservationNumber(reservation.getReservationNumber());
        return ResponseEntity.ok(response);
    }

    @Data
    public static class PublicReservationRequest {
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

    @Data
    public static class ReservationResponse {
        private String reservationNumber;
    }
}
