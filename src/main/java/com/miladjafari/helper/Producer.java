package com.miladjafari.helper;

import com.miladjafari.price.PriceDto;
import com.miladjafari.price.PriceService;

import java.util.ArrayList;
import java.util.List;

public class Producer {

    private final PriceService priceService;
    private final Integer chunksOfRecords;

    public Producer(PriceService priceService, Integer chunksOfRecords) {
        this.priceService = priceService;
        this.chunksOfRecords = chunksOfRecords;
    }

    public void uploadPrices(List<PriceDto> prices) {
        priceService.beginBatchTransaction();

        List<List<PriceDto>> pricePartitions = partitionsThePrices(prices);
        pricePartitions.forEach(partition -> {
            partition.forEach(priceService::save);
        });

        priceService.commitBatchTransaction();
    }


    public List<List<PriceDto>> partitionsThePrices(List<PriceDto> prices) {
        List<List<PriceDto>> partitions = new ArrayList<>();
        if (prices.isEmpty()) {
            return partitions;
        }

        int fromIndex = 0;
        int numberOfRecordsPerPartitions = prices.size() < chunksOfRecords ? partitions.size() : chunksOfRecords;
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
