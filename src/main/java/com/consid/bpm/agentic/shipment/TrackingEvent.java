package com.consid.bpm.agentic.shipment;

import java.time.OffsetDateTime;

public record TrackingEvent(OffsetDateTime occurredAt, String location, String description) {

    public String summary() {
        return "%s, %s (%s)".formatted(description, location, occurredAt.toInstant());
    }
}
