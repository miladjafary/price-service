package com.miladjafari.helper;

import com.miladjafari.price.InMemoryPriceService;
import com.miladjafari.price.PriceDto;
import com.miladjafari.price.PriceService;
import com.miladjafari.util.PriceTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ProducerTest {

    private static final int CHUNK_OF_RECORDS = 1000;

    private PriceService priceService;
    private List<PriceDto> mockPrice;

    @BeforeEach
    public void before() {
        priceService = new InMemoryPriceService();
        mockPrice = PriceTestUtils.createMockPriceLists(2500);
    }

    @Test
    public void shouldUploadPrices() {
        //GIVEN
        Producer producer = new Producer(priceService, CHUNK_OF_RECORDS);

        //WHEN
        producer.uploadPrices(mockPrice);

        //THEN
        assertThat(priceService.findAllPrices().size()).isEqualTo(mockPrice.size());
    }

}