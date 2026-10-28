package com.consid.bpm.agentic.order;

import com.consid.bpm.agentic.common.Inputs;
import com.consid.bpm.agentic.customer.CustomerService;
import com.consid.bpm.agentic.order.OrderSearchResult.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final String ID_PREFIX = "ORD";

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository, CustomerService customerService, Clock clock) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.clock = clock;
    }

    /// Finds an order by an id as typed by a customer, e.g. `"10002"` finds `"ORD-10002"`.
    public Optional<Order> findOrder(String rawOrderId) {
        return Inputs.normalizeId(rawOrderId, ID_PREFIX).flatMap(orderRepository::findById);
    }

    /// All orders without their items, newest first.
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    /// Orders with the given status without their items, newest first.
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /// The customer's orders with their items, newest first.
    public List<Order> findByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    /// Finds the order a customer is asking about. An existing order id wins if it belongs to the customer,
    /// otherwise the customer's orders are narrowed down by the order date.
    public OrderSearchResult search(OrderQuery query) {
        OrderSearchResult result = searchByOrderId(query).orElseGet(() -> searchByCustomer(query));
        log.info("Order search for {}: {} {}", query, result.outcome(), result.message());
        return result;
    }

    public OrderStatusResult getStatus(String rawOrderId) {
        OrderStatusResult result = findOrder(rawOrderId)
                .map(OrderStatusResult::of)
                .orElseGet(() -> OrderStatusResult.notFound(rawOrderId));
        log.info("Order status for {}: {}", rawOrderId, result.orderStatus());
        return result;
    }

    private Optional<OrderSearchResult> searchByOrderId(OrderQuery query) {
        return findOrder(query.orderId()).map(order -> belongsToGivenCustomer(order, query.customerId())
                ? OrderSearchResult.found(Outcome.FOUND_BY_ORDER_ID, order, "Order %s found by its id".formatted(order.id()))
                : OrderSearchResult.notFound(Outcome.CUSTOMER_MISMATCH,
                        "Order %s exists but does not belong to customer %s".formatted(order.id(), query.customerId())));
    }

    /// `true` if no customer was given, so an order id alone is enough.
    private boolean belongsToGivenCustomer(Order order, String rawCustomerId) {
        return customerService.parseCustomerId(rawCustomerId).map(order.customerId()::equals).orElse(true);
    }

    private OrderSearchResult searchByCustomer(OrderQuery query) {
        Optional<String> customerId = customerService.parseCustomerId(query.customerId());
        if (customerId.isEmpty()) {
            return OrderSearchResult.notFound(Outcome.NOT_FOUND, "The order id is unknown and no customer id was given");
        }
        if (!customerService.exists(customerId.get())) {
            return OrderSearchResult.notFound(Outcome.UNKNOWN_CUSTOMER, "There is no customer with id " + customerId.get());
        }
        List<Order> orders = orderRepository.findByCustomerId(customerId.get());
        if (orders.isEmpty()) {
            return OrderSearchResult.notFound(Outcome.NO_ORDERS, "Customer %s has no orders".formatted(customerId.get()));
        }
        return OrderDates.parse(query.orderDate(), LocalDate.now(clock))
                .map(date -> OrderMatcher.byDate(orders, date))
                .orElseGet(() -> OrderMatcher.withoutDate(orders));
    }
}
