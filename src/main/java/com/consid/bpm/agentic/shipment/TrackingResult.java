package com.consid.bpm.agentic.shipment;

/// Result of the `get-tracking-code` tool.
///
/// @param arrivalTime the actual delivery time for delivered shipments, otherwise the (revised) estimate
public record TrackingResult(String orderId, TrackingStatus trackingStatus, String trackingNumber, String provider,
                             String trackingUrl, String arrivalTime, String lastEvent, String message) {

    public enum TrackingStatus {
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELAYED,
        DELIVERED,
        NOT_SHIPPED,
        ORDER_NOT_FOUND
    }

    static TrackingResult unavailable(String orderId, TrackingStatus status, String message) {
        return new TrackingResult(orderId, status, null, null, null, null, null, message);
    }
}
