package com.consid.bpm.agentic.console;

import com.consid.bpm.agentic.customer.Customer;
import com.consid.bpm.agentic.notification.Notification;

/// @param customer `null` if the notification went to an unknown customer
public record NotificationRow(Notification notification, Customer customer) {
}
