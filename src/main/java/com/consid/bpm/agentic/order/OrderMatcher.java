package com.consid.bpm.agentic.order;

import com.consid.bpm.agentic.order.OrderSearchResult.Outcome;

import java.time.LocalDate;
import java.util.List;

/// Picks the order a customer most likely means from their orders. Pure logic without I/O.
final class OrderMatcher {

    static final long MAX_DATE_TOLERANCE_DAYS = 2;

    private OrderMatcher() {
    }

    /// Prefers an order on the exact date, then the single closest order within [#MAX_DATE_TOLERANCE_DAYS].
    static OrderSearchResult byDate(List<Order> orders, LocalDate date) {
        List<Order> sameDay = ordersDaysApart(orders, date, 0);
        if (sameDay.size() == 1) {
            return OrderSearchResult.found(Outcome.FOUND_BY_ORDER_DATE, sameDay.getFirst(),
                    "Order %s was placed on %s".formatted(sameDay.getFirst().id(), date));
        }
        if (sameDay.size() > 1) {
            return OrderSearchResult.notFound(Outcome.MULTIPLE_MATCHES, sameDay,
                    "%d orders were placed on %s".formatted(sameDay.size(), date));
        }

        long closestDistance = orders.stream().mapToLong(order -> order.daysApartFrom(date)).min().orElseThrow();
        if (closestDistance > MAX_DATE_TOLERANCE_DAYS) {
            return OrderSearchResult.notFound(Outcome.NOT_FOUND, orders, "No order was placed on or around %s".formatted(date));
        }
        List<Order> closest = ordersDaysApart(orders, date, closestDistance);
        if (closest.size() > 1) {
            return OrderSearchResult.notFound(Outcome.MULTIPLE_MATCHES, closest,
                    "No order on %s, but %d orders %d day(s) apart".formatted(date, closest.size(), closestDistance));
        }
        Order order = closest.getFirst();
        return OrderSearchResult.found(Outcome.FOUND_NEAR_ORDER_DATE, order,
                "No order on %s, but order %s was placed on %s".formatted(date, order.id(), order.orderDate()));
    }

    /// Without a date only an unambiguous order can be picked: the only order or the only open one.
    static OrderSearchResult withoutDate(List<Order> orders) {
        if (orders.size() == 1) {
            return OrderSearchResult.found(Outcome.FOUND_ONLY_ORDER, orders.getFirst(),
                    "Order %s is the customer's only order".formatted(orders.getFirst().id()));
        }
        List<Order> open = orders.stream().filter(order -> !order.status().isClosed()).toList();
        if (open.size() == 1) {
            return OrderSearchResult.found(Outcome.FOUND_ONLY_OPEN_ORDER, open.getFirst(),
                    "Order %s is the customer's only open order".formatted(open.getFirst().id()));
        }
        return OrderSearchResult.notFound(Outcome.MULTIPLE_MATCHES, orders,
                "The customer has %d orders, an order date is needed to pick one".formatted(orders.size()));
    }

    private static List<Order> ordersDaysApart(List<Order> orders, LocalDate date, long days) {
        return orders.stream().filter(order -> order.daysApartFrom(date) == days).toList();
    }
}
