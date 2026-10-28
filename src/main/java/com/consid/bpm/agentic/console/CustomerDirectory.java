package com.consid.bpm.agentic.console;

import com.consid.bpm.agentic.customer.Customer;
import com.consid.bpm.agentic.customer.CustomerService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/// Resolves customer ids to customers so list views can show names instead of ids.
@Component
class CustomerDirectory {

    private final CustomerService customerService;

    CustomerDirectory(CustomerService customerService) {
        this.customerService = customerService;
    }

    Map<String, Customer> byId() {
        return customerService.findAll().stream().collect(Collectors.toMap(Customer::id, Function.identity()));
    }
}
