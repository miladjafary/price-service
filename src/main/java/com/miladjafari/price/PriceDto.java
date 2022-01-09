package com.miladjafari.price;

import java.time.LocalDateTime;

public class PriceDto {
    private String id;
    private LocalDateTime asOf;
    private Double price;

    public PriceDto(String id, LocalDateTime asOf, Double price) {
        this.id = id;
        this.asOf = asOf;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getAsOf() {
        return asOf;
    }

    public void setAsOf(LocalDateTime asOf) {
        this.asOf = asOf;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
