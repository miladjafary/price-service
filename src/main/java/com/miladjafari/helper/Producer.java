package com.miladjafari.helper;

import com.miladjafari.price.PriceDto;
import com.miladjafari.price.PriceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Producer {
    private static final Logger logger = LogManager.getLogger();

    private final PriceService priceService;
    private final Integer chunksOfRecords;

    public Producer(PriceService priceService) {
        this.priceService = priceService;
        this.chunksOfRecords = 1000;
    }

    public Producer(PriceService priceService, Integer chunksOfRecords) {
        this.priceService = priceService;
        this.chunksOfRecords = chunksOfRecords;
    }

    public void uploadPrices(List<PriceDto> prices) {
        logger.info("Number of prices to be imported: " + prices.size());
        priceService.beginBatchTransaction();

        List<List<PriceDto>> pricePartitions = partitionsThePrices(prices);
        pricePartitions.forEach(partition -> {
            partition.forEach(priceService::save);
        });

        priceService.commitBatchTransaction();

        logger.info(priceService.findAllPrices().size() + " prices has been imported");
    }


    public List<List<PriceDto>> partitionsThePrices(List<PriceDto> prices) {
        List<List<PriceDto>> partitions = new ArrayList<>();
        if (prices.isEmpty()) {
            return partitions;
        }

        int fromIndex = 0;
        int numberOfRecordsPerPartitions = prices.size() < chunksOfRecords ? prices.size() : chunksOfRecords;
        int numberOfPartitions = prices.size() / numberOfRecordsPerPartitions;
        for (int i = 0; i < numberOfPartitions; i++) {
            int toIndex = fromIndex + numberOfRecordsPerPartitions;

            partitions.add(prices.subList(fromIndex, toIndex));

            fromIndex = toIndex;
        }

        if (fromIndex < prices.size()) {
            partitions.add(prices.subList(fromIndex, prices.size()));
        }

        return partitions;
    }
}
