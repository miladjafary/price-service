package com.miladjafari.helper;

import com.miladjafari.price.InMemoryPriceService;
import com.miladjafari.price.PriceDto;
import com.miladjafari.price.PriceService;
import com.miladjafari.util.PriceTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ConsumerTest {

    private PriceService priceService;
    private List<PriceDto> mockPrice;

    @BeforeEach
    public void before() {
        priceService = new InMemoryPriceService();
        mockPrice = PriceTestUtils.createMockPriceLists();

        priceService.beginBatchTransaction();
        mockPrice.forEach(priceService::save);
        priceService.commitBatchTransaction();
    }


    @Test
    public void shouldReturnPriceWhenGivenIdIsExist() {
        //GIVEN
        PriceDto expectedPrice = mockPrice.get(0);
        String priceId = expectedPrice.getId();

        //WHEN
        Consumer consumer = new Consumer(priceService);
        Optional<PriceDto> actualPrice = consumer.getLatestPriceById(priceId);

        //THEN
        assertThat(actualPrice.isPresent()).isTrue();
        assertThat(actualPrice.get()).usingRecursiveComparison().isEqualTo(expectedPrice);
    }

    @Test
    public void shouldNotReturnPriceWhenGivenIdIsNotExist() {
        //GIVEN
        String priceId = "NOT_EXIST_PRICE_ID";

        //WHEN
        Consumer consumer = new Consumer(priceService);
        Optional<PriceDto> actualPrice = consumer.getLatestPriceById(priceId);

        //THEN
        assertThat(actualPrice.isPresent()).isFalse();
    }

}