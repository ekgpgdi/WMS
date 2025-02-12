package com.dahye.wms.customer.service;

import com.dahye.wms.common.exception.NotFoundException;
import com.dahye.wms.customer.domain.Customer;
import com.dahye.wms.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public Customer get(Long customerId) {
        return customerRepository.findById(customerId).orElseThrow(() -> new NotFoundException("NOT_FOUND_CUSTOMER"));
    }
}
