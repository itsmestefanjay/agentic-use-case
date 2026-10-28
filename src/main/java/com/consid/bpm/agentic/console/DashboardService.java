package com.consid.bpm.agentic.console;

import com.consid.bpm.agentic.console.Dashboard.Attention;
import com.consid.bpm.agentic.customer.Customer;
import com.consid.bpm.agentic.notification.NotificationService;
import com.consid.bpm.agentic.order.Order;
import com.consid.bpm.agentic.order.OrderService;
import com.consid.bpm.agentic.order.OrderStatus;
import com.consid.bpm.agentic.shipment.Shipment;
import com.consid.bpm.agentic.shipment.TrackingService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
class DashboardService {

    private static final Set<OrderStatus> ATTENTION_STATUSES = Set.of(OrderStatus.ON_HOLD, OrderStatus.PAYMENT_PENDING);
    private static final Set<OrderStatus> IN_TRANSIT_STATUSES = Set.of(OrderStatus.SHIPPED, OrderStatus.OUT_FOR_DELIVERY);
    private static final int LATEST_NOTIFICATIONS = 5;

    private final OrderService orderService;
    private final TrackingService trackingService;
    private final NotificationService notificationService;
    private final CustomerDirectory customerDirectory;

    DashboardService(OrderService orderService, TrackingService trackingService, NotificationService notificationService,
                     CustomerDirectory customerDirectory) {
        this.orderService = orderService;
        this.trackingService = trackingService;
        this.notificationService = notificationService;
        this.customerDirectory = customerDirectory;
    }

    Dashboard load() {
        Map<String, Customer> customers = customerDirectory.byId();
        Map<String, Order> orders = orderService.findAll().stream().collect(Collectors.toMap(Order::id, Function.identity()));
        List<Shipment> overdue = trackingService.findOverdueShipments();

        List<Attention> needsAttention = Stream.concat(
                overdue.stream().map(shipment -> attention(orders.get(shipment.orderId()), customers,
                        "Delayed, the estimated delivery has passed")),
                orders.values().stream()
                        .filter(order -> ATTENTION_STATUSES.contains(order.status()))
                        .map(order -> attention(order, customers, order.status().description())))
                .toList();

        return new Dashboard(
                customers.size(),
                orders.values().stream().filter(order -> !order.status().isClosed()).count(),
                orders.values().stream().filter(order -> IN_TRANSIT_STATUSES.contains(order.status())).count(),
                overdue.size(),
                notificationService.count(),
                needsAttention,
                notificationService.findLatest(LATEST_NOTIFICATIONS).stream()
                        .map(notification -> new NotificationRow(notification, customers.get(notification.customerId())))
                        .toList());
    }

    private static Attention attention(Order order, Map<String, Customer> customers, String reason) {
        return new Attention(order, customers.get(order.customerId()), reason);
    }
}
