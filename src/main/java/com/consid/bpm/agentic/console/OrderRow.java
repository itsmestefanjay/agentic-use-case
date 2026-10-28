package com.consid.bpm.agentic.console;

import com.consid.bpm.agentic.customer.Customer;
import com.consid.bpm.agentic.order.Order;

public record OrderRow(Order order, Customer customer) {
}
