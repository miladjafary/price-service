package com.miladjafari;

import com.miladjafari.helper.Consumer;
import com.miladjafari.helper.Producer;
import com.miladjafari.price.InMemoryPriceService;
import com.miladjafari.price.PriceDto;
import com.miladjafari.price.PriceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws InterruptedException {
        PriceService priceService = new InMemoryPriceService();

        List<PriceDto> pricesForProducer1 = createPriceLists(200);
        Runnable producer1 = () -> {
            Producer producer = new Producer(priceService);
            producer.uploadPrices(pricesForProducer1);
        };

        List<PriceDto> pricesForProducer2 = createPriceLists(10);
        Runnable producer2 = () -> {
            Producer producer = new Producer(priceService, 10);
            producer.uploadPrices(pricesForProducer2);
        };

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(producer1);
        executorService.submit(producer2);

        Thread.sleep(2000);
        logger.info("Number of imported price:" + priceService.findAllPrices().size());

        Optional<PriceDto> priceOptional =  new Consumer(priceService).getLatestPriceById(pricesForProducer1.get(0).getId());
        priceOptional.ifPresent(price->{
            logger.info("Price Id:" + price.getId());
            logger.info("Price AsOf: "+ price.getAsOf());
            logger.info("Price value: "+ price.getPrice());
        });

        executorService.shutdown();
    }

    public static List<PriceDto> createPriceLists(Integer number) {
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
