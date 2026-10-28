package com.consid.bpm.agentic.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/// Input of the `search-order` tool. All values are free text as provided by the customer.
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderQuery(String orderId, String customerId, String orderDate) {
}
