package com.consid.bpm.agentic.customer;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class CustomerRepository {

    private final JdbcClient jdbc;

    public CustomerRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Customer> findById(String id) {
        return jdbc.sql("SELECT * FROM customers WHERE id = ?")
                .param(id)
                .query(Customer.class)
                .optional();
    }

    public List<Customer> findAll() {
        return jdbc.sql("SELECT * FROM customers ORDER BY name")
                .query(Customer.class)
                .list();
    }

    /// Customers whose id, name or email contains the text, ignoring case.
    public List<Customer> search(String text) {
        return jdbc.sql("""
                        SELECT * FROM customers
                        WHERE LOWER(id) LIKE :pattern OR LOWER(name) LIKE :pattern OR LOWER(email) LIKE :pattern
                        ORDER BY name""")
                .param("pattern", "%" + text.strip().toLowerCase(Locale.ROOT) + "%")
                .query(Customer.class)
                .list();
    }

    public boolean existsById(String id) {
        return jdbc.sql("SELECT COUNT(*) FROM customers WHERE id = ?")
                .param(id)
                .query(Integer.class)
                .single() > 0;
    }

    public void save(Customer customer) {
        jdbc.sql("""
                        INSERT INTO customers (id, name, email, phone, preferred_channel, language, tier)
                        VALUES (?, ?, ?, ?, ?, ?, ?)""")
                .params(customer.id(), customer.name(), customer.email(), customer.phone(),
                        customer.preferredChannel().name(), customer.language(), customer.tier().name())
                .update();
    }
}
