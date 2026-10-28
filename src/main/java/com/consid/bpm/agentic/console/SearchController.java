package com.consid.bpm.agentic.console;

import com.consid.bpm.agentic.customer.CustomerService;
import com.consid.bpm.agentic.order.OrderService;
import com.consid.bpm.agentic.shipment.TrackingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/// Global search: jumps straight to an order, customer or tracked shipment,
/// otherwise falls back to the customer list filtered by the text.
@Controller
class SearchController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final TrackingService trackingService;

    SearchController(OrderService orderService, CustomerService customerService, TrackingService trackingService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.trackingService = trackingService;
    }

    @GetMapping("/search")
    String search(@RequestParam(defaultValue = "") String q, RedirectAttributes redirect) {
        return orderService.findOrder(q).map(order -> "redirect:/orders/" + order.id())
                .or(() -> customerService.findCustomer(q).map(customer -> "redirect:/customers/" + customer.id()))
                .or(() -> trackingService.findByTrackingNumber(q).map(shipment -> "redirect:/orders/" + shipment.orderId()))
                .orElseGet(() -> {
                    redirect.addAttribute("q", q.strip());
                    return "redirect:/customers";
                });
    }
}
