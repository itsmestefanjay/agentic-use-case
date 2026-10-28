package com.consid.bpm.agentic.notification;

import com.consid.bpm.agentic.customer.Customer;
import com.consid.bpm.agentic.customer.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/// Mock notification gateway: picks the customer's preferred channel and stores the message instead of sending it.
@Component
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private static final int MAX_MESSAGE_LENGTH = 4000;

    private final CustomerService customerService;
    private final NotificationRepository notificationRepository;
    private final Clock clock;

    public NotificationService(CustomerService customerService, NotificationRepository notificationRepository, Clock clock) {
        this.customerService = customerService;
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    /// @param rawCustomerId the customer to notify; unknown customers are notified via the [NotificationChannel#PORTAL]
    public NotificationResult informCustomer(String rawCustomerId, StatusMessage statusMessage) {
        if (statusMessage.message() == null || statusMessage.message().isBlank()) {
            log.warn("Rejected an empty message to customer {}", rawCustomerId);
            return NotificationResult.rejected("The message must not be empty");
        }

        Optional<Customer> customer = customerService.findCustomer(rawCustomerId);
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                customer.map(Customer::id).orElse(null),
                customer.map(c -> NotificationChannel.of(c.preferredChannel())).orElse(NotificationChannel.PORTAL),
                customer.map(Customer::preferredAddress).orElse(null),
                statusMessage.orderStatus(),
                truncate(statusMessage.message().strip()),
                OffsetDateTime.now(clock));
        notificationRepository.save(notification);
        log.info("Sent {} notification to customer {}: {}", notification.channel(), notification.customerId(), notification.message());
        return NotificationResult.sent(notification);
    }

    /// The latest notifications, newest first.
    public List<Notification> findLatest(int limit) {
        return notificationRepository.findLatest(limit);
    }

    public List<Notification> findByCustomer(String customerId) {
        return notificationRepository.findByCustomerId(customerId);
    }

    public int count() {
        return notificationRepository.count();
    }

    private static String truncate(String message) {
        return message.length() > MAX_MESSAGE_LENGTH ? message.substring(0, MAX_MESSAGE_LENGTH) : message;
    }
}
