package com.miladjafari;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
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
