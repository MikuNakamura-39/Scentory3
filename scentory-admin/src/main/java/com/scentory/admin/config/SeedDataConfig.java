package com.scentory.admin.config;

import com.scentory.admin.entity.Customer;
import com.scentory.admin.entity.FaqEntry;
import com.scentory.admin.entity.Notice;
import com.scentory.admin.entity.Reservation;
import com.scentory.admin.entity.Staff;
import com.scentory.admin.entity.SystemSetting;
import com.scentory.admin.entity.TimeSlot;
import com.scentory.admin.entity.UserAccount;
import com.scentory.admin.entity.WorkshopMenu;
import com.scentory.admin.model.ReservationStatus;
import com.scentory.admin.model.RoleType;
import com.scentory.admin.repository.CustomerRepository;
import com.scentory.admin.repository.FaqEntryRepository;
import com.scentory.admin.repository.NoticeRepository;
import com.scentory.admin.repository.ReservationRepository;
import com.scentory.admin.repository.StaffRepository;
import com.scentory.admin.repository.SystemSettingRepository;
import com.scentory.admin.repository.TimeSlotRepository;
import com.scentory.admin.repository.UserAccountRepository;
import com.scentory.admin.repository.WorkshopMenuRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedData(
        UserAccountRepository userAccountRepository,
        StaffRepository staffRepository,
        CustomerRepository customerRepository,
        WorkshopMenuRepository workshopMenuRepository,
        TimeSlotRepository timeSlotRepository,
        ReservationRepository reservationRepository,
        NoticeRepository noticeRepository,
        FaqEntryRepository faqEntryRepository,
        SystemSettingRepository systemSettingRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userAccountRepository.count() > 0) {
                return;
            }

            UserAccount admin = new UserAccount();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setDisplayName("Scentory Admin");
            admin.setRoleType(RoleType.ADMIN);
            userAccountRepository.save(admin);

            UserAccount staffUser = new UserAccount();
            staffUser.setUsername("staff");
            staffUser.setPassword(passwordEncoder.encode("staff123"));
            staffUser.setDisplayName("Scentory Staff");
            staffUser.setRoleType(RoleType.STAFF);
            userAccountRepository.save(staffUser);

            UserAccount viewerUser = new UserAccount();
            viewerUser.setUsername("viewer");
            viewerUser.setPassword(passwordEncoder.encode("viewer123"));
            viewerUser.setDisplayName("Scentory Viewer");
            viewerUser.setRoleType(RoleType.VIEWER);
            userAccountRepository.save(viewerUser);

            Staff ayaka = new Staff();
            ayaka.setName("Ayaka");
            ayaka.setEmail("ayaka@scentory.jp");
            ayaka.setPhone("03-1111-2222");
            ayaka.setRoleType(RoleType.ADMIN);
            staffRepository.save(ayaka);

            Staff ren = new Staff();
            ren.setName("Ren");
            ren.setEmail("ren@scentory.jp");
            ren.setPhone("03-1111-3333");
            ren.setRoleType(RoleType.STAFF);
            staffRepository.save(ren);

            WorkshopMenu standard = new WorkshopMenu();
            standard.setName("オーダーフレグランス体験");
            standard.setPrice(new BigDecimal("8800"));
            standard.setDurationMinutes(90);
            standard.setDescription("数十種類の香料から選ぶ、Scentory の標準コースです。");
            workshopMenuRepository.save(standard);

            Customer customer = new Customer();
            customer.setFullName("山田 花子");
            customer.setEmail("hanako@example.com");
            customer.setPhone("090-1234-5678");
            customer.setChannel("Web");
            customer.setTags("初回来店");
            customer.setVisitCount(1);
            customerRepository.save(customer);

            TimeSlot slot = new TimeSlot();
            slot.setStartAt(LocalDateTime.now().withHour(11).withMinute(0).plusDays(1));
            slot.setEndAt(LocalDateTime.now().withHour(12).withMinute(30).plusDays(1));
            slot.setCapacity(4);
            slot.setBookedCount(2);
            slot.setSlotType("通常枠");
            slot.setStaff(ayaka);
            slot.setWorkshopMenu(standard);
            timeSlotRepository.save(slot);

            Reservation reservation = new Reservation();
            reservation.setReservationNumber("RSV-202603-001");
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setPartySize(2);
            reservation.setChannel("Web");
            reservation.setCustomer(customer);
            reservation.setTimeSlot(slot);
            reservation.setWorkshopMenu(standard);
            reservation.setAssignedStaff(ayaka);
            reservationRepository.save(reservation);

            Notice notice = new Notice();
            notice.setTitle("4月営業カレンダー更新");
            notice.setBody("新しい予約枠を追加しました。");
            notice.setPinned(true);
            notice.setPublishedAt(LocalDateTime.now().minusDays(1));
            noticeRepository.save(notice);

            FaqEntry faq = new FaqEntry();
            faq.setQuestion("キャンセルはいつまで可能ですか？");
            faq.setAnswer("当日までのキャンセルは不可です。");
            faqEntryRepository.save(faq);

            SystemSetting setting = new SystemSetting();
            setting.setSettingKey("reservation.default-capacity");
            setting.setSettingValue("4");
            systemSettingRepository.save(setting);
        };
    }
}
