package com.miladjafari;



import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        OldService priceService = new OldService();

        executorService.submit(()->{
            priceService.startBatchTransaction();
            priceService.uploadPrice();
        });

        Runnable runnable = ()-> priceService.getPriceBy("");

        executorService.submit(runnable);
        executorService.submit(runnable);
    }
}
