package com.consid.bpm.agentic.console;

import com.consid.bpm.agentic.customer.Customer;
import com.consid.bpm.agentic.customer.CustomerService;
import com.consid.bpm.agentic.notification.NotificationService;
import com.consid.bpm.agentic.order.Order;
import com.consid.bpm.agentic.order.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customers")
class CustomerController {

    private final CustomerService customerService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    CustomerController(CustomerService customerService, OrderService orderService, NotificationService notificationService) {
        this.customerService = customerService;
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    @GetMapping
    String customers(@RequestParam(defaultValue = "") String q, Model model) {
        Map<String, List<Order>> ordersByCustomer = orderService.findAll().stream()
                .collect(Collectors.groupingBy(Order::customerId));
        List<CustomerRow> rows = customerService.search(q).stream()
                .map(customer -> CustomerRow.of(customer, ordersByCustomer.getOrDefault(customer.id(), List.of())))
                .toList();
        model.addAttribute("q", q);
        model.addAttribute("customers", rows);
        return "customers";
    }

    @GetMapping("/{customerId}")
    String customer(@PathVariable String customerId, Model model) {
        Customer customer = customerService.findCustomer(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown customer " + customerId));
        model.addAttribute("customer", customer);
        model.addAttribute("orders", orderService.findByCustomer(customer.id()));
        model.addAttribute("notifications", notificationService.findByCustomer(customer.id()));
        return "customer";
    }
}
