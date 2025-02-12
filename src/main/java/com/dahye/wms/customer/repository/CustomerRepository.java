package com.dahye.wms.customer.repository;

import com.dahye.wms.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {
    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
}
