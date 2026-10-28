package com.consid.bpm.agentic.customer;

/// Result of the `verify-customer` tool.
///
/// @param nameMatches `null` when no name was given
public record CustomerVerification(boolean customerExists, String customerId, Boolean nameMatches, CustomerTier tier,
                                   String message) {

    static CustomerVerification missingId() {
        return new CustomerVerification(false, null, null, null, "No customer id was provided");
    }

    static CustomerVerification unknown(String customerId) {
        return new CustomerVerification(false, customerId, null, null, "There is no customer with id " + customerId);
    }

    static CustomerVerification exists(Customer customer, Boolean nameMatches) {
        String message = Boolean.FALSE.equals(nameMatches)
                ? "Customer %s exists, but the given name does not match the registered name".formatted(customer.id())
                : "Customer %s exists".formatted(customer.id());
        return new CustomerVerification(true, customer.id(), nameMatches, customer.tier(), message);
    }
}
