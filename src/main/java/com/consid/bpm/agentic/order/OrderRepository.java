package com.consid.bpm.agentic.order;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private final JdbcClient jdbc;

    public OrderRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Order> findById(String id) {
        return jdbc.sql("SELECT * FROM orders WHERE id = ?")
                .param(id)
                .query(OrderRepository::mapOrder)
                .optional()
                .map(this::withItems);
    }

    /// The customer's orders, newest first.
    public List<Order> findByCustomerId(String customerId) {
        return jdbc.sql("SELECT * FROM orders WHERE customer_id = ? ORDER BY order_date DESC, id DESC")
                .param(customerId)
                .query(OrderRepository::mapOrder)
                .stream()
                .map(this::withItems)
                .toList();
    }

    /// All orders without their items, newest first.
    public List<Order> findAll() {
        return jdbc.sql("SELECT * FROM orders ORDER BY order_date DESC, id DESC")
                .query(OrderRepository::mapOrder)
                .list();
    }

    /// Orders with the given status without their items, newest first.
    public List<Order> findByStatus(OrderStatus status) {
        return jdbc.sql("SELECT * FROM orders WHERE status = ? ORDER BY order_date DESC, id DESC")
                .param(status.name())
                .query(OrderRepository::mapOrder)
                .list();
    }

    public void save(Order order) {
        jdbc.sql("""
                        INSERT INTO orders (id, customer_id, order_date, status, status_updated_at, total, currency, shipping_city, shipping_country)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""")
                .params(order.id(), order.customerId(), order.orderDate(), order.status().name(), order.statusUpdatedAt(),
                        order.total(), order.currency(), order.shippingCity(), order.shippingCountry())
                .update();
        for (OrderItem item : order.items()) {
            jdbc.sql("INSERT INTO order_items (order_id, sku, name, quantity, unit_price) VALUES (?, ?, ?, ?, ?)")
                    .params(order.id(), item.sku(), item.name(), item.quantity(), item.unitPrice())
                    .update();
        }
    }

    private Order withItems(Order order) {
        List<OrderItem> items = jdbc.sql("SELECT sku, name, quantity, unit_price FROM order_items WHERE order_id = ?")
                .param(order.id())
                .query(OrderItem.class)
                .list();
        return order.withItems(items);
    }

    private static Order mapOrder(ResultSet rs, int rowNum) throws SQLException {
        return new Order(
                rs.getString("id"),
                rs.getString("customer_id"),
                rs.getObject("order_date", LocalDate.class),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getObject("status_updated_at", OffsetDateTime.class),
                rs.getBigDecimal("total"),
                rs.getString("currency"),
                rs.getString("shipping_city"),
                rs.getString("shipping_country"),
                List.of());
    }
}
