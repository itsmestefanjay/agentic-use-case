package com.consid.bpm.agentic.shipment;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/// @param deliveredAt `null` while the shipment is on its way
/// @param events      tracking events, oldest first
public record Shipment(String orderId, Carrier carrier, String trackingNumber, OffsetDateTime shippedAt,
                       OffsetDateTime estimatedDelivery, OffsetDateTime deliveredAt, List<TrackingEvent> events) {

    public Shipment {
        events = List.copyOf(events);
    }

    public Shipment withEvents(List<TrackingEvent> events) {
        return new Shipment(orderId, carrier, trackingNumber, shippedAt, estimatedDelivery, deliveredAt, events);
    }

    public boolean isDelivered() {
        return deliveredAt != null;
    }

    /// Not delivered although the estimated delivery time has passed.
    public boolean isOverdue(OffsetDateTime now) {
        return !isDelivered() && estimatedDelivery.isBefore(now);
    }

    public List<TrackingEvent> eventsNewestFirst() {
        return events.reversed();
    }

    public Optional<TrackingEvent> lastEvent() {
        return events.isEmpty() ? Optional.empty() : Optional.of(events.getLast());
    }

    public String trackingUrl() {
        return carrier.trackingUrl(trackingNumber);
    }
}
