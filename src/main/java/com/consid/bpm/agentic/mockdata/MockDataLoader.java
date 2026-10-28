package com.consid.bpm.agentic.mockdata;

import com.consid.bpm.agentic.customer.Customer;
import com.consid.bpm.agentic.customer.CustomerRepository;
import com.consid.bpm.agentic.order.Order;
import com.consid.bpm.agentic.order.OrderItem;
import com.consid.bpm.agentic.order.OrderRepository;
import com.consid.bpm.agentic.order.OrderStatus;
import com.consid.bpm.agentic.shipment.Carrier;
import com.consid.bpm.agentic.shipment.Shipment;
import com.consid.bpm.agentic.shipment.ShipmentRepository;
import com.consid.bpm.agentic.shipment.TrackingEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/// Loads the mock customers and orders from `data/*.json` into the in-memory database on startup.
/// All dates in the JSON are relative to now, so the demo data never goes stale.
/// Runs during bean initialization, i.e. before the job workers start polling.
@Component
@DependsOnDatabaseInitialization
public class MockDataLoader implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(MockDataLoader.class);

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final Clock clock;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MockDataLoader(CustomerRepository customerRepository, OrderRepository orderRepository,
                          ShipmentRepository shipmentRepository, Clock clock) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
        this.clock = clock;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        List<Customer> customers = read("data/customers.json", new TypeReference<>() {});
        List<SeedOrder> orders = read("data/orders.json", new TypeReference<>() {});

        OffsetDateTime now = OffsetDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);
        customers.forEach(customerRepository::save);
        for (SeedOrder seed : orders) {
            orderRepository.save(seed.toOrder(now));
            if (seed.shipment() != null) {
                shipmentRepository.save(seed.shipment().toShipment(seed.id(), now));
            }
        }

        log.info("Loaded {} customers and {} orders into the in-memory database", customers.size(), orders.size());
        orderRepository.findAll().forEach(order -> log.info("  {}  customer={}  orderDate={}  status={}",
                order.id(), order.customerId(), order.orderDate(), order.status()));
    }

    private <T> T read(String path, TypeReference<T> type) throws IOException {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(in, type);
        }
    }

    record SeedOrder(String id, String customerId, int orderedDaysAgo, OrderStatus status, int statusUpdatedHoursAgo,
                     String currency, String shippingCity, String shippingCountry, List<OrderItem> items,
                     SeedShipment shipment) {

        Order toOrder(OffsetDateTime now) {
            BigDecimal total = items.stream().map(OrderItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            LocalDate orderDate = now.toLocalDate().minusDays(orderedDaysAgo);
            return new Order(id, customerId, orderDate, status, now.minusHours(statusUpdatedHoursAgo), total, currency,
                    shippingCity, shippingCountry, items);
        }
    }

    record SeedShipment(Carrier carrier, String trackingNumber, int shippedHoursAgo, int estimatedDeliveryInHours,
                        Integer deliveredHoursAgo, List<SeedEvent> events) {

        Shipment toShipment(String orderId, OffsetDateTime now) {
            OffsetDateTime deliveredAt = deliveredHoursAgo == null ? null : now.minusHours(deliveredHoursAgo);
            List<TrackingEvent> trackingEvents = events.stream()
                    .map(event -> new TrackingEvent(now.minusHours(event.hoursAgo()), event.location(), event.description()))
                    .toList();
            return new Shipment(orderId, carrier, trackingNumber, now.minusHours(shippedHoursAgo),
                    now.plusHours(estimatedDeliveryInHours), deliveredAt, trackingEvents);
        }
    }

    record SeedEvent(int hoursAgo, String location, String description) {
    }
}
