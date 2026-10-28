package com.consid.bpm.agentic.order;

import java.util.List;

/// Result of the `search-order` tool.
///
/// @param orderId    the matched order, `null` if no single order could be identified
/// @param candidates the orders the customer might mean when the search was ambiguous
public record OrderSearchResult(boolean found, Outcome outcome, String orderId, List<Candidate> candidates, String message) {

    public enum Outcome {
        FOUND_BY_ORDER_ID,
        FOUND_BY_ORDER_DATE,
        FOUND_NEAR_ORDER_DATE,
        FOUND_ONLY_ORDER,
        FOUND_ONLY_OPEN_ORDER,
        MULTIPLE_MATCHES,
        CUSTOMER_MISMATCH,
        UNKNOWN_CUSTOMER,
        NO_ORDERS,
        NOT_FOUND
    }

    public record Candidate(String orderId, String orderDate, OrderStatus orderStatus) {

        static Candidate of(Order order) {
            return new Candidate(order.id(), order.orderDate().toString(), order.status());
        }
    }

    public OrderSearchResult {
        candidates = List.copyOf(candidates);
    }

    static OrderSearchResult found(Outcome outcome, Order order, String message) {
        return new OrderSearchResult(true, outcome, order.id(), List.of(), message);
    }

    static OrderSearchResult notFound(Outcome outcome, String message) {
        return notFound(outcome, List.of(), message);
    }

    static OrderSearchResult notFound(Outcome outcome, List<Order> candidates, String message) {
        return new OrderSearchResult(false, outcome, null, candidates.stream().map(Candidate::of).toList(), message);
    }
}
