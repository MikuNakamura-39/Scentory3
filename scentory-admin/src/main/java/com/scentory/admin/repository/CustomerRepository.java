package com.scentory.admin.repository;

import com.scentory.admin.entity.Customer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByFullNameContainingIgnoreCase(String keyword);
    Optional<Customer> findFirstByFullNameIgnoreCase(String fullName);
    Optional<Customer> findFirstByEmailIgnoreCase(String email);
}
