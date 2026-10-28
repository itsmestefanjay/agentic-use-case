package com.consid.bpm.agentic.notification;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class NotificationRepository {

    private final JdbcClient jdbc;

    public NotificationRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /// The latest notifications, newest first.
    public List<Notification> findLatest(int limit) {
        return jdbc.sql("SELECT * FROM notifications ORDER BY sent_at DESC LIMIT ?")
                .param(limit)
                .query(NotificationRepository::mapNotification)
                .list();
    }

    public List<Notification> findByCustomerId(String customerId) {
        return jdbc.sql("SELECT * FROM notifications WHERE customer_id = ? ORDER BY sent_at DESC")
                .param(customerId)
                .query(NotificationRepository::mapNotification)
                .list();
    }

    public int count() {
        return jdbc.sql("SELECT COUNT(*) FROM notifications").query(Integer.class).single();
    }

    public void save(Notification notification) {
        jdbc.sql("""
                        INSERT INTO notifications (id, customer_id, channel, recipient, order_status, message, sent_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?)""")
                .params(notification.id(), notification.customerId(), notification.channel().name(), notification.recipient(),
                        notification.orderStatus(), notification.message(), notification.sentAt())
                .update();
    }

    private static Notification mapNotification(ResultSet rs, int rowNum) throws SQLException {
        return new Notification(
                rs.getString("id"),
                rs.getString("customer_id"),
                NotificationChannel.valueOf(rs.getString("channel")),
                rs.getString("recipient"),
                rs.getString("order_status"),
                rs.getString("message"),
                rs.getObject("sent_at", OffsetDateTime.class));
    }
}
