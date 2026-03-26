package com.scentory.admin.config;

import com.scentory.admin.entity.Customer;
import com.scentory.admin.entity.Staff;
import com.scentory.admin.entity.TimeSlot;
import com.scentory.admin.entity.WorkshopMenu;
import com.scentory.admin.repository.CustomerRepository;
import com.scentory.admin.repository.StaffRepository;
import com.scentory.admin.repository.TimeSlotRepository;
import com.scentory.admin.repository.WorkshopMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final WorkshopMenuRepository workshopMenuRepository;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, Customer.class,
            source -> source == null || source.isBlank() ? null : customerRepository.findById(Long.valueOf(source)).orElse(null));
        registry.addConverter(Customer.class, String.class,
            source -> source == null || source.getId() == null ? "" : source.getId().toString());

        registry.addConverter(String.class, Staff.class,
            source -> source == null || source.isBlank() ? null : staffRepository.findById(Long.valueOf(source)).orElse(null));
        registry.addConverter(Staff.class, String.class,
            source -> source == null || source.getId() == null ? "" : source.getId().toString());

        registry.addConverter(String.class, TimeSlot.class,
            source -> source == null || source.isBlank() ? null : timeSlotRepository.findById(Long.valueOf(source)).orElse(null));
        registry.addConverter(TimeSlot.class, String.class,
            source -> source == null || source.getId() == null ? "" : source.getId().toString());

        registry.addConverter(String.class, WorkshopMenu.class,
            source -> source == null || source.isBlank() ? null : workshopMenuRepository.findById(Long.valueOf(source)).orElse(null));
        registry.addConverter(WorkshopMenu.class, String.class,
            source -> source == null || source.getId() == null ? "" : source.getId().toString());
    }
}
