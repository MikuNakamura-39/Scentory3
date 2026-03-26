package com.scentory.admin.repository;

import com.scentory.admin.entity.FaqEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqEntryRepository extends JpaRepository<FaqEntry, Long> {
}
