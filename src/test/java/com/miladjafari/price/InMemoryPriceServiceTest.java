package com.miladjafari.price;


import com.miladjafari.excpetion.TransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InMemoryPriceServiceTest {

    private PriceService priceService;
    private List<PriceDto> mockPrices = new ArrayList<>();

    @BeforeEach
    public void before() {
        priceService = new InMemoryPriceService();
        mockPrices = createMockPriceLists();
    }

    private List<PriceDto> createMockPriceLists() {
        Random randomPrice = new Random();
        List<PriceDto> prices = new ArrayList<>();

        for (long step = 0; step < 10; step++) {
            prices.add(new PriceDto(
                    UUID.randomUUID().toString(),
                    LocalDateTime.now().plusMinutes(step),
                    randomPrice.nextDouble())
            );
        }

        return prices;
    }

    @Test
    public void shouldSavePricesWhenTransactionIsCommitted() {
        priceService.beginBatchTransaction();
        mockPrices.forEach(priceService::save);
        priceService.commitBatchTransaction();

        assertThat(priceService.findAllPrices().size()).isEqualTo(mockPrices.size());
    }

    @Test
    public void shouldNotSavePricesWhenTransactionIsNotCommit() {
        PriceService priceService = new InMemoryPriceService();
        priceService.beginBatchTransaction();
        mockPrices.forEach(priceService::save);

        assertThat(priceService.findAllPrices().isEmpty()).isTrue();
    }

    @Test
    public void shouldOnlySavePricesOfAThreadWhenBatchTransactionIsCommittedThere() throws InterruptedException {
        Integer expectedPriceListSize = mockPrices.size();

        Runnable thread1WhichCommitTheBatchTransaction = () -> {
            priceService.beginBatchTransaction();
            mockPrices.forEach(priceService::save);
            priceService.commitBatchTransaction();
        };

        Runnable thread2WhichDoesNotCommitTheBatchTransaction = () -> {
            priceService.beginBatchTransaction();
            PriceDto priceDto = new PriceDto(UUID.randomUUID().toString(), LocalDateTime.now().plusMinutes(10), 1000D);
            priceService.save(priceDto);
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(thread1WhichCommitTheBatchTransaction);
        executorService.submit(thread2WhichDoesNotCommitTheBatchTransaction);

        Thread.sleep(1000);

        assertThat(priceService.findAllPrices().size()).isEqualTo(expectedPriceListSize);
    }

    @Test
    public void shouldSavePricesWhenBothThreadsCommittedBatchTransaction() throws InterruptedException {
        Integer expectedPriceListSize = mockPrices.size() + 1;

        Runnable thread1WhichSaveAllTheMockPrices = () -> {
            priceService.beginBatchTransaction();
            mockPrices.forEach(priceService::save);
            priceService.commitBatchTransaction();
        };

        Runnable thread2WhichOnlyOnePrice = () -> {
            priceService.beginBatchTransaction();
            PriceDto priceDto = new PriceDto(UUID.randomUUID().toString(), LocalDateTime.now().plusMinutes(10), 1000D);
            priceService.save(priceDto);
            priceService.commitBatchTransaction();
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(thread1WhichSaveAllTheMockPrices);
        executorService.submit(thread2WhichOnlyOnePrice);

        Thread.sleep(1000);

        assertThat(priceService.findAllPrices().size()).isEqualTo(expectedPriceListSize);
    }

    @Test
    public void shouldResilientAgainstProducersWhenCallTheServiceMethodsInAnIncorrectOrder() {
        PriceDto priceDto = new PriceDto(UUID.randomUUID().toString(), LocalDateTime.now().plusMinutes(10), 1000D);

        assertThrows(TransactionException.class, ()-> priceService.rollBackBatchTransaction());
        assertThrows(TransactionException.class, ()-> priceService.commitBatchTransaction());
        assertThrows(TransactionException.class, ()-> priceService.save(priceDto));
    }


}