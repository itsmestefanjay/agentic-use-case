package com.consid.bpm.agentic.notification;

import java.time.OffsetDateTime;

/// @param customerId `null` if the customer is unknown
/// @param recipient  `null` for the [NotificationChannel#PORTAL]
public record Notification(String id, String customerId, NotificationChannel channel, String recipient, String orderStatus,
                           String message, OffsetDateTime sentAt) {
}
