package com.miladjafari.price;

import java.util.List;
import java.util.Optional;

/**
 * An interface for keep tracking of the latest prices for financial instruments.
 */
public interface PriceService {

    /**
     * Return all persisted prices
     * @return list of PriceDto
     */
    List<PriceDto> findAllPrices();

    /**
     * Return price by given <code>id</code> if existed.
     * @param id given the price id
     * @return Optional of PriceDto
     */
    Optional<PriceDto> findById(String id);

    /**
     * Use for staring a transaction inside a thread.
     */
    void beginBatchTransaction();

    /**
     * Save the priceDto only in the thread transaction without touching the persisted prices inside datastore.
     * @param priceDto
     */
    void save(PriceDto priceDto);

    /**
     * Store the existing prices in the thread transaction into the datastore
     */
    void commitBatchTransaction();

    /**
     * Rolled back the thread transaction without touching the prices datastore.
     */
    void rollBackBatchTransaction();
}
