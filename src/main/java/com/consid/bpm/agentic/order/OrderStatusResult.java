package com.consid.bpm.agentic.order;

import java.util.List;

/// Result of the `order-status` tool.
///
/// @param orderStatus       the [OrderStatus] name, or `STATUS_NOT_FOUND`
/// @param trackingAvailable whether the order has been handed over to a carrier
public record OrderStatusResult(String orderId, String orderStatus, String statusDescription, String orderDate,
                                String lastUpdated, List<String> items, String total, boolean trackingAvailable) {

    static final String STATUS_NOT_FOUND = "STATUS_NOT_FOUND";

    public OrderStatusResult {
        items = List.copyOf(items);
    }

    static OrderStatusResult of(Order order) {
        return new OrderStatusResult(
                order.id(),
                order.status().name(),
                order.status().description(),
                order.orderDate().toString(),
                order.statusUpdatedAt().toInstant().toString(),
                order.items().stream().map(OrderItem::summary).toList(),
                order.total() + " " + order.currency(),
                order.status().isShipped());
    }

    static OrderStatusResult notFound(String orderId) {
        return new OrderStatusResult(orderId, STATUS_NOT_FOUND, "No order found with this id", null, null, List.of(), null, false);
    }
}
