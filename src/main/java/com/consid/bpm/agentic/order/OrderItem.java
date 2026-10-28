package com.consid.bpm.agentic.order;

import java.math.BigDecimal;

public record OrderItem(String sku, String name, int quantity, BigDecimal unitPrice) {

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String summary() {
        return quantity + " x " + name;
    }
}
