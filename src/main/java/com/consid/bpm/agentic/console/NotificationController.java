package com.consid.bpm.agentic.console;

import com.consid.bpm.agentic.customer.Customer;
import com.consid.bpm.agentic.notification.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
class NotificationController {

    private static final int MAX_NOTIFICATIONS = 200;

    private final NotificationService notificationService;
    private final CustomerDirectory customerDirectory;

    NotificationController(NotificationService notificationService, CustomerDirectory customerDirectory) {
        this.notificationService = notificationService;
        this.customerDirectory = customerDirectory;
    }

    @GetMapping("/notifications")
    String notifications(Model model) {
        Map<String, Customer> customers = customerDirectory.byId();
        model.addAttribute("notifications", notificationService.findLatest(MAX_NOTIFICATIONS).stream()
                .map(notification -> new NotificationRow(notification, customers.get(notification.customerId())))
                .toList());
        return "notifications";
    }
}
