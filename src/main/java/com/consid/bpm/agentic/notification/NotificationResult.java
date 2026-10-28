package com.consid.bpm.agentic.notification;

/// Result of the `inform-customer` tool.
///
/// @param recipient the masked address the message was sent to, `null` for the customer portal
public record NotificationResult(String notificationId, DeliveryStatus deliveryStatus, NotificationChannel channel,
                                 String recipient, String sentAt, String message) {

    public enum DeliveryStatus {
        SENT,
        REJECTED
    }

    static NotificationResult sent(Notification notification) {
        return new NotificationResult(notification.id(), DeliveryStatus.SENT, notification.channel(),
                mask(notification.recipient()), notification.sentAt().toInstant().toString(), notification.message());
    }

    static NotificationResult rejected(String reason) {
        return new NotificationResult(null, DeliveryStatus.REJECTED, null, null, null, reason);
    }

    /// Hides personal data from the process variables: `a***@example.com`, `***1002`.
    private static String mask(String recipient) {
        if (recipient == null) {
            return null;
        }
        int at = recipient.indexOf('@');
        return at > 0
                ? recipient.charAt(0) + "***" + recipient.substring(at)
                : "***" + recipient.substring(Math.max(0, recipient.length() - 4));
    }
}
