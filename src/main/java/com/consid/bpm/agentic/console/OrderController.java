package com.consid.bpm.agentic.console;

import com.consid.bpm.agentic.customer.Customer;
import com.consid.bpm.agentic.customer.CustomerService;
import com.consid.bpm.agentic.order.Order;
import com.consid.bpm.agentic.order.OrderService;
import com.consid.bpm.agentic.order.OrderStatus;
import com.consid.bpm.agentic.shipment.TrackingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final TrackingService trackingService;
    private final CustomerDirectory customerDirectory;

    OrderController(OrderService orderService, CustomerService customerService, TrackingService trackingService,
                    CustomerDirectory customerDirectory) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.trackingService = trackingService;
        this.customerDirectory = customerDirectory;
    }

    @GetMapping
    String orders(@RequestParam(required = false) OrderStatus status, Model model) {
        List<Order> all = orderService.findAll();
        Map<OrderStatus, Long> counts = all.stream().collect(
                Collectors.groupingBy(Order::status, () -> new EnumMap<>(OrderStatus.class), Collectors.counting()));
        Map<String, Customer> customers = customerDirectory.byId();
        List<OrderRow> rows = all.stream()
                .filter(order -> status == null || order.status() == status)
                .map(order -> new OrderRow(order, customers.get(order.customerId())))
                .toList();
        model.addAttribute("status", status);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("counts", counts);
        model.addAttribute("total", all.size());
        model.addAttribute("orders", rows);
        return "orders";
    }

    @GetMapping("/{orderId}")
    String order(@PathVariable String orderId, Model model) {
        Order order = orderService.findOrder(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown order " + orderId));
        model.addAttribute("order", order);
        model.addAttribute("customer", customerService.findCustomer(order.customerId()).orElse(null));
        model.addAttribute("shipment", trackingService.findShipment(order.id()).orElse(null));
        model.addAttribute("tracking", trackingService.getTrackingInfo(order.id()));
        return "order";
    }
}
