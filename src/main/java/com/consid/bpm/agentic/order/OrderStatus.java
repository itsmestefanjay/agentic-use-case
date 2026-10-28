package com.consid.bpm.agentic.order;

public enum OrderStatus {
    PAYMENT_PENDING("Waiting for the payment to be confirmed", false, false),
    CONFIRMED("Order confirmed and waiting to be picked in the warehouse", false, false),
    IN_PREPARATION("Being picked and packed in the warehouse", false, false),
    ON_HOLD("On hold, customer service has to clarify details such as the shipping address", false, false),
    SHIPPED("Handed over to the carrier and on its way", true, false),
    OUT_FOR_DELIVERY("Out for delivery today", true, false),
    DELIVERED("Delivered to the customer", true, true),
    CANCELLED("Cancelled, nothing will be shipped", false, true),
    RETURNED("Returned by the customer", true, true);

    private final String description;
    private final boolean shipped;
    private final boolean closed;

    OrderStatus(String description, boolean shipped, boolean closed) {
        this.description = description;
        this.shipped = shipped;
        this.closed = closed;
    }

    public String description() {
        return description;
    }

    /// Whether the order has been handed over to a carrier, i.e. tracking information exists.
    public boolean isShipped() {
        return shipped;
    }

    /// Whether nothing more will happen with the order.
    public boolean isClosed() {
        return closed;
    }
}
