package com.miladjafari.price;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriceDto priceDto = (PriceDto) o;
        return Objects.equals(id, priceDto.id) && Objects.equals(asOf, priceDto.asOf) && Objects.equals(price, priceDto.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, asOf, price);
    }
}
