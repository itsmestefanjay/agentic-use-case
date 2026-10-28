package com.consid.bpm.agentic.shipment;

import com.consid.bpm.agentic.common.Inputs;
import com.consid.bpm.agentic.order.Order;
import com.consid.bpm.agentic.order.OrderService;
import com.consid.bpm.agentic.order.OrderStatus;
import com.consid.bpm.agentic.shipment.TrackingResult.TrackingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Component
public class TrackingService {

    private static final Logger log = LoggerFactory.getLogger(TrackingService.class);

    /// How much later than now an overdue shipment is expected to arrive.
    private static final Duration OVERDUE_REESTIMATE = Duration.ofDays(1);

    private final OrderService orderService;
    private final ShipmentRepository shipmentRepository;
    private final Clock clock;

    public TrackingService(OrderService orderService, ShipmentRepository shipmentRepository, Clock clock) {
        this.orderService = orderService;
        this.shipmentRepository = shipmentRepository;
        this.clock = clock;
    }

    public TrackingResult getTrackingInfo(String rawOrderId) {
        TrackingResult result = orderService.findOrder(rawOrderId)
                .map(this::track)
                .orElseGet(() -> TrackingResult.unavailable(rawOrderId, TrackingStatus.ORDER_NOT_FOUND, "No order found with this id"));
        log.info("Tracking info for {}: {}", rawOrderId, result.trackingStatus());
        return result;
    }

    public Optional<Shipment> findShipment(String orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }

    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return Inputs.isMissing(trackingNumber) ? Optional.empty() : shipmentRepository.findByTrackingNumber(trackingNumber);
    }

    /// Undelivered shipments whose estimated delivery time has passed.
    public List<Shipment> findOverdueShipments() {
        return shipmentRepository.findOverdue(OffsetDateTime.now(clock));
    }

    private TrackingResult track(Order order) {
        return shipmentRepository.findByOrderId(order.id())
                .filter(_ -> order.status().isShipped())
                .map(shipment -> track(order, shipment))
                .orElseGet(() -> TrackingResult.unavailable(order.id(), TrackingStatus.NOT_SHIPPED,
                        "Order %s has status %s and has not been shipped yet".formatted(order.id(), order.status())));
    }

    private TrackingResult track(Order order, Shipment shipment) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        if (shipment.isDelivered()) {
            return result(shipment, TrackingStatus.DELIVERED, shipment.deliveredAt(), "The shipment was delivered");
        }
        if (shipment.isOverdue(now)) {
            return result(shipment, TrackingStatus.DELAYED, now.plus(OVERDUE_REESTIMATE).truncatedTo(ChronoUnit.MINUTES),
                    "The shipment is delayed, the original estimate was " + shipment.estimatedDelivery().toInstant());
        }
        TrackingStatus status = order.status() == OrderStatus.OUT_FOR_DELIVERY ? TrackingStatus.OUT_FOR_DELIVERY : TrackingStatus.IN_TRANSIT;
        return result(shipment, status, shipment.estimatedDelivery(), "The shipment is on its way");
    }

    private static TrackingResult result(Shipment shipment, TrackingStatus status, OffsetDateTime arrivalTime, String message) {
        return new TrackingResult(
                shipment.orderId(),
                status,
                shipment.trackingNumber(),
                shipment.carrier().displayName(),
                shipment.trackingUrl(),
                arrivalTime.toInstant().toString(),
                shipment.lastEvent().map(TrackingEvent::summary).orElse(null),
                message);
    }
}
