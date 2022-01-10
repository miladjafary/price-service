package com.miladjafari.helper;

import com.miladjafari.price.PriceDto;
import com.miladjafari.price.PriceService;

import java.util.Optional;

public class Consumer {

    private final PriceService priceService;

    public Consumer(PriceService priceService) {
        this.priceService = priceService;
    }

    public Optional<PriceDto> getLatestPriceById(String priceId) {
        return priceService.findById(priceId);
    }
}
