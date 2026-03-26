package com.scentory.admin.repository;

import com.scentory.admin.entity.WorkshopMenu;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkshopMenuRepository extends JpaRepository<WorkshopMenu, Long> {
    Optional<WorkshopMenu> findFirstByNameIgnoreCase(String name);
    Optional<WorkshopMenu> findFirstByActiveTrueOrderByIdAsc();
}
