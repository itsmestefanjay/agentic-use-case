package com.consid.bpm.agentic.customer;

import com.consid.bpm.agentic.common.Inputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private static final String ID_PREFIX = "C";

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /// Normalizes a customer id as typed by a customer, e.g. `"1001"` becomes `"C-1001"`.
    public Optional<String> parseCustomerId(String raw) {
        return Inputs.normalizeId(raw, ID_PREFIX);
    }

    public Optional<Customer> findCustomer(String rawCustomerId) {
        return parseCustomerId(rawCustomerId).flatMap(customerRepository::findById);
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    /// Customers whose id, name or email contains the text; all customers if the text is empty.
    public List<Customer> search(String text) {
        return Inputs.isMissing(text) ? customerRepository.findAll() : customerRepository.search(text);
    }

    public boolean exists(String customerId) {
        return customerRepository.existsById(customerId);
    }

    public CustomerVerification verify(CustomerQuery query) {
        CustomerVerification result = parseCustomerId(query.customerId())
                .map(customerId -> customerRepository.findById(customerId)
                        .map(customer -> verifyName(customer, query.name()))
                        .orElseGet(() -> CustomerVerification.unknown(customerId)))
                .orElseGet(CustomerVerification::missingId);
        log.info("Customer verification for {}: {}", query, result.message());
        return result;
    }

    private static CustomerVerification verifyName(Customer customer, String givenName) {
        Boolean nameMatches = Inputs.isMissing(givenName) ? null : customer.hasName(givenName);
        return CustomerVerification.exists(customer, nameMatches);
    }
}
