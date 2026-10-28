package com.consid.bpm.agentic.notification;

import com.consid.bpm.agentic.customer.ContactChannel;

public enum NotificationChannel {
    EMAIL,
    SMS,
    /// The inbox in the customer portal, used when the customer is unknown.
    PORTAL;

    static NotificationChannel of(ContactChannel channel) {
        return switch (channel) {
            case EMAIL -> EMAIL;
            case SMS -> SMS;
        };
    }
}
