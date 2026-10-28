package com.consid.bpm.agentic.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/// Input of the `verify-customer` tool.
@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerQuery(String customerId, String name) {
}
