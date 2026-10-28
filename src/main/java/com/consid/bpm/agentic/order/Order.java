package com.consid.bpm.agentic.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record Order(String id, String customerId, LocalDate orderDate, OrderStatus status, OffsetDateTime statusUpdatedAt,
                    BigDecimal total, String currency, String shippingCity, String shippingCountry, List<OrderItem> items) {

    public Order {
        items = List.copyOf(items);
    }

    public Order withItems(List<OrderItem> items) {
        return new Order(id, customerId, orderDate, status, statusUpdatedAt, total, currency, shippingCity, shippingCountry, items);
    }

    public long daysApartFrom(LocalDate date) {
        return Math.abs(ChronoUnit.DAYS.between(orderDate, date));
    }
}
