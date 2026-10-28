package com.consid.bpm.agentic.shipment;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ShipmentRepository {

    private final JdbcClient jdbc;

    public ShipmentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Shipment> findByOrderId(String orderId) {
        return jdbc.sql("SELECT * FROM shipments WHERE order_id = ?")
                .param(orderId)
                .query(ShipmentRepository::mapShipment)
                .optional()
                .map(shipment -> shipment.withEvents(findEvents(orderId)));
    }

    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return jdbc.sql("SELECT * FROM shipments WHERE UPPER(tracking_number) = UPPER(?)")
                .param(trackingNumber.strip())
                .query(ShipmentRepository::mapShipment)
                .optional()
                .map(shipment -> shipment.withEvents(findEvents(shipment.orderId())));
    }

    /// Undelivered shipments whose estimated delivery time has passed, without their events.
    public List<Shipment> findOverdue(OffsetDateTime now) {
        return jdbc.sql("SELECT * FROM shipments WHERE delivered_at IS NULL AND estimated_delivery < ? ORDER BY estimated_delivery")
                .param(now)
                .query(ShipmentRepository::mapShipment)
                .list();
    }

    public void save(Shipment shipment) {
        jdbc.sql("""
                        INSERT INTO shipments (order_id, carrier, tracking_number, shipped_at, estimated_delivery, delivered_at)
                        VALUES (?, ?, ?, ?, ?, ?)""")
                .params(shipment.orderId(), shipment.carrier().name(), shipment.trackingNumber(), shipment.shippedAt(),
                        shipment.estimatedDelivery(), shipment.deliveredAt())
                .update();
        for (TrackingEvent event : shipment.events()) {
            jdbc.sql("INSERT INTO tracking_events (order_id, occurred_at, location, description) VALUES (?, ?, ?, ?)")
                    .params(shipment.orderId(), event.occurredAt(), event.location(), event.description())
                    .update();
        }
    }

    private List<TrackingEvent> findEvents(String orderId) {
        return jdbc.sql("SELECT occurred_at, location, description FROM tracking_events WHERE order_id = ? ORDER BY occurred_at")
                .param(orderId)
                .query((rs, _) -> new TrackingEvent(
                        rs.getObject("occurred_at", OffsetDateTime.class),
                        rs.getString("location"),
                        rs.getString("description")))
                .list();
    }

    private static Shipment mapShipment(ResultSet rs, int rowNum) throws SQLException {
        return new Shipment(
                rs.getString("order_id"),
                Carrier.valueOf(rs.getString("carrier")),
                rs.getString("tracking_number"),
                rs.getObject("shipped_at", OffsetDateTime.class),
                rs.getObject("estimated_delivery", OffsetDateTime.class),
                rs.getObject("delivered_at", OffsetDateTime.class),
                List.of());
    }
}
