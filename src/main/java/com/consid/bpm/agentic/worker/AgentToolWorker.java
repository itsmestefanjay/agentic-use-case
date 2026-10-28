package com.consid.bpm.agentic.worker;

import com.consid.bpm.agentic.customer.CustomerQuery;
import com.consid.bpm.agentic.customer.CustomerService;
import com.consid.bpm.agentic.customer.CustomerVerification;
import com.consid.bpm.agentic.notification.NotificationResult;
import com.consid.bpm.agentic.notification.NotificationService;
import com.consid.bpm.agentic.notification.StatusMessage;
import com.consid.bpm.agentic.order.OrderQuery;
import com.consid.bpm.agentic.order.OrderSearchResult;
import com.consid.bpm.agentic.order.OrderService;
import com.consid.bpm.agentic.order.OrderStatusResult;
import com.consid.bpm.agentic.shipment.TrackingResult;
import com.consid.bpm.agentic.shipment.TrackingService;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import org.springframework.stereotype.Component;

/// Job workers for the tools of the AI agent in the `Order Tools` ad-hoc sub-process.
///
/// Workers contain no logic: they bind the tool's input variables, delegate to a service
/// and return its result record, which becomes the job's result variables.
@Component
public class AgentToolWorker {

    private final CustomerService customerService;
    private final OrderService orderService;
    private final TrackingService trackingService;
    private final NotificationService notificationService;

    public AgentToolWorker(CustomerService customerService, OrderService orderService, TrackingService trackingService,
                           NotificationService notificationService) {
        this.customerService = customerService;
        this.orderService = orderService;
        this.trackingService = trackingService;
        this.notificationService = notificationService;
    }

    @JobWorker(type = "verify-customer", fetchVariables = "customerInfo", streamEnabled = false)
    public CustomerVerification verifyCustomer(@Variable CustomerQuery customerInfo) {
        return customerService.verify(customerInfo);
    }

    @JobWorker(type = "search-order", fetchVariables = "orderInfo", streamEnabled = false)
    public OrderSearchResult searchOrder(@Variable OrderQuery orderInfo) {
        return orderService.search(orderInfo);
    }

    @JobWorker(type = "order-status", fetchVariables = "orderId", streamEnabled = false)
    public OrderStatusResult getOrderStatus(@Variable String orderId) {
        return orderService.getStatus(orderId);
    }

    @JobWorker(type = "get-tracking-code", fetchVariables = "orderId", streamEnabled = false)
    public TrackingResult getTrackingCode(@Variable String orderId) {
        return trackingService.getTrackingInfo(orderId);
    }

    /// Also fetches the process variable `customerId` from the start form to address the customer.
    @JobWorker(type = "inform-customer", fetchVariables = {"statusMessage", "customerId"}, streamEnabled = false)
    public NotificationResult informCustomer(@Variable StatusMessage statusMessage, @Variable(optional = true) String customerId) {
        return notificationService.informCustomer(customerId, statusMessage);
    }
}
