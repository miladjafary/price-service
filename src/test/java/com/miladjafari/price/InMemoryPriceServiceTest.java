package com.miladjafari.price;


import com.miladjafari.excpetion.TransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        //WHEN
        priceService.beginBatchTransaction();
        mockPrices.forEach(priceService::save);
        priceService.commitBatchTransaction();

        //THEN
        assertThat(priceService.findAllPrices().size()).isEqualTo(mockPrices.size());
    }

    @Test
    public void shouldReturnAllPricesWhenTransactionIsCommitted() {
        //WHEN
        priceService.beginBatchTransaction();
        mockPrices.forEach(priceService::save);
        priceService.commitBatchTransaction();

        //THEN
        List<PriceDto> actualPrices = priceService.findAllPrices();
        assertThat(actualPrices.toArray()).isEqualTo(mockPrices.toArray());
    }

    @Test
    public void shouldNotSavePricesWhenTransactionIsNotCommit() {
        //WHEN
        priceService.beginBatchTransaction();
        mockPrices.forEach(priceService::save);

        //THEN
        assertThat(priceService.findAllPrices().isEmpty()).isTrue();
    }

    @Test
    public void shouldNotSavePricesWhenTransactionIsRolledBack() {
        //WHEN
        priceService.beginBatchTransaction();
        mockPrices.forEach(priceService::save);
        priceService.rollBackBatchTransaction();

        //THEN
        assertThat(priceService.findAllPrices().isEmpty()).isTrue();
    }

    @Test
    public void shouldOnlySavePricesOfAThreadWhenBatchTransactionIsCommittedThere() throws InterruptedException {
        //GIVEN
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

        //WHEN
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(thread1WhichCommitTheBatchTransaction);
        executorService.submit(thread2WhichDoesNotCommitTheBatchTransaction);

        Thread.sleep(1000);

        //THEN
        assertThat(priceService.findAllPrices().size()).isEqualTo(expectedPriceListSize);
    }

    @Test
    public void shouldSavePricesWhenBothThreadsCommittedBatchTransaction() throws InterruptedException {
        //GIVEN
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

        //WHEN
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(thread1WhichSaveAllTheMockPrices);
        executorService.submit(thread2WhichOnlyOnePrice);

        Thread.sleep(1000);

        //THEN
        assertThat(priceService.findAllPrices().size()).isEqualTo(expectedPriceListSize);
    }

    @Test
    public void shouldResilientAgainstProducersWhenCallTheServiceMethodsInAnIncorrectOrder() {
        //GIVEN
        PriceDto priceDto = new PriceDto(UUID.randomUUID().toString(), LocalDateTime.now().plusMinutes(10), 1000D);

        //WHEN
        assertThrows(TransactionException.class, () -> priceService.rollBackBatchTransaction());
        assertThrows(TransactionException.class, () -> priceService.commitBatchTransaction());
        assertThrows(TransactionException.class, () -> priceService.save(priceDto));
    }

    @Test
    public void shouldThrowExceptionWhenTransactionIsAlreadyCompletedAndItIsRequestedToOpenAgain() {
        //GIVEN
        PriceDto priceDto = new PriceDto(UUID.randomUUID().toString(), LocalDateTime.now().plusMinutes(10), 1000D);
        priceService.beginBatchTransaction();
        priceService.save(priceDto);
        priceService.commitBatchTransaction();

        //WHEN
        assertThrows(TransactionException.class, () -> priceService.beginBatchTransaction());
    }

    @Test
    public void shouldReturnTheLatestPriceForGivenIdWhenThereAreMultipleOfItWithDifferentAsOfDateTime() {
        //GIVEN
        String priceId = UUID.randomUUID().toString();
        mockPrices = new ArrayList<PriceDto>() {{
            add(new PriceDto(priceId, LocalDateTime.now().plusDays(1), 30D)); //expected
            add(new PriceDto(priceId, LocalDateTime.now(), 20D));
            add(new PriceDto(priceId, LocalDateTime.now().minusDays(1), 10D));
            add(new PriceDto(UUID.randomUUID().toString(), LocalDateTime.now(), 10D));
        }};
        PriceDto expectedPrice = mockPrices.get(0);

        priceService.beginBatchTransaction();
        mockPrices.forEach(priceService::save);
        priceService.commitBatchTransaction();

        //WHEN
        Optional<PriceDto> actualPrice = priceService.findById(priceId);

        //THEN
        assertThat(actualPrice.isPresent()).isTrue();
        assertThat(actualPrice.get()).usingRecursiveComparison().isEqualTo(expectedPrice);
    }

    @Test
    public void shouldReturnTheLatestPriceForGivenIdesWhenTwoThreadAreModifyingThePrices() throws InterruptedException {
        //GIVEN
        String priceId = UUID.randomUUID().toString();
        mockPrices = new ArrayList<PriceDto>() {{
            add(new PriceDto(priceId, LocalDateTime.now().plusDays(1), 30D)); //expected
            add(new PriceDto(priceId, LocalDateTime.now(), 20D));
            add(new PriceDto(priceId, LocalDateTime.now().minusDays(1), 10D));
            add(new PriceDto(UUID.randomUUID().toString(), LocalDateTime.now(), 10D));
        }};
        PriceDto expectedPrice = mockPrices.get(0);

        Runnable producer1 = () -> {
            priceService.beginBatchTransaction();
            mockPrices.forEach(priceService::save);
            priceService.commitBatchTransaction();
        };

        Runnable producer2 = () -> {
            priceService.beginBatchTransaction();
            PriceDto priceDto = new PriceDto(UUID.randomUUID().toString(), LocalDateTime.now().plusMinutes(10), 1000D);
            priceService.save(priceDto);
            priceService.commitBatchTransaction();
        };

        //WHEN
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(producer1);
        executorService.submit(producer2);

        Thread.sleep(2000);
        Optional<PriceDto> actualPrice = priceService.findById(priceId);

        //THEN
        assertThat(actualPrice.isPresent()).isTrue();
        assertThat(actualPrice.get()).usingRecursiveComparison().isEqualTo(expectedPrice);
    }
}