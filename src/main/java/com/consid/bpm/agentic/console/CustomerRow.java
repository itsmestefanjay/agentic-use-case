package com.consid.bpm.agentic.console;

import com.consid.bpm.agentic.customer.Customer;
import com.consid.bpm.agentic.order.Order;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/// @param lastOrderDate `null` if the customer has no orders
public record CustomerRow(Customer customer, int orders, long openOrders, LocalDate lastOrderDate) {

    static CustomerRow of(Customer customer, List<Order> orders) {
        return new CustomerRow(
                customer,
                orders.size(),
                orders.stream().filter(order -> !order.status().isClosed()).count(),
                orders.stream().map(Order::orderDate).max(Comparator.naturalOrder()).orElse(null));
    }
}
