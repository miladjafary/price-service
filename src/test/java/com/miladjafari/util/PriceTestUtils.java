package com.miladjafari.util;

import com.miladjafari.price.PriceDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class PriceTestUtils {
    public static List<PriceDto> createMockPriceLists() {
        return createMockPriceLists(10);
    }

    public static List<PriceDto> createMockPriceLists(Integer number) {
        Random randomPrice = new Random();
        List<PriceDto> prices = new ArrayList<>();

        for (long step = 0; step < number; step++) {
            prices.add(new PriceDto(
                    UUID.randomUUID().toString(),
                    LocalDateTime.now().plusMinutes(step),
                    randomPrice.nextDouble())
            );
        }

        return prices;
    }
}
