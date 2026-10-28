package com.consid.bpm.agentic.console;

import com.consid.bpm.agentic.customer.Customer;
import com.consid.bpm.agentic.order.Order;

import java.util.List;

/// Read model of the dashboard page.
public record Dashboard(int customers, long openOrders, long inTransit, int delayedShipments, int notifications,
                 List<Attention> needsAttention, List<NotificationRow> latestNotifications) {

    /// An order a support agent should look at, and why.
    public record Attention(Order order, Customer customer, String reason) {
    }
}
