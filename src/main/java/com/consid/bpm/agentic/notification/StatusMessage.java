package com.consid.bpm.agentic.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/// Input of the `inform-customer` tool, written by the AI agent.
@JsonIgnoreProperties(ignoreUnknown = true)
public record StatusMessage(String orderStatus, String message) {
}
