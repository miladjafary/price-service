package com.miladjafari.price;

import com.miladjafari.excpetion.TransactionException;
import com.miladjafari.helper.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * An in-memory implementation of {@link PriceService} for keep tracking of the latest prices for financial instruments.
 */
public class InMemoryPriceService implements PriceService {
    private static final Logger logger = LogManager.getLogger();


    /**
     * Transaction class is used to keep track of each individual thread activities for modifying the prices.
     */
    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();

    /**
     * Acquiring lock to prevent read prices while a writing thread is trying to modify the prices
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Use a list as a datastore for keeping track of prices
     */
    private final List<PriceDto> prices = new ArrayList<>();


    /**
     * Return all persisted prices
     *
     * @return list of PriceDto
     */
    @Override
    public List<PriceDto> findAllPrices() {
        return new ArrayList<>(prices);
    }

    /**
     * Return price by given <code>id</code> if existed. Acquire the read lock to prevent read dirty date.
     *
     * @param id
     * @return Optional of PriceDto
     */
    @Override
    public Optional<PriceDto> findById(String id) {
        Optional<PriceDto> price;

        lock.readLock().lock();
        try {
            price = findAllPrices().stream()
                    .filter(p -> p.getId().equals(id))
                    .sorted(Comparator.comparing(PriceDto::getAsOf).reversed())
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }

        return price;
    }

    /**
     * Use for staring a transaction inside a thread.
     */
    @Override
    public void beginBatchTransaction() {
        String threadTransactionId = getThreadTransactionId();
        if (hasThreadAnyTransaction()) {
            logger.error(String.format("Thread %s has already a transaction", threadTransactionId));
        } else {
            transactions.put(threadTransactionId, new Transaction(threadTransactionId));
            transactions.get(threadTransactionId).begin();
            logger.info(String.format("The batch transaction has been created for [%s] thread", threadTransactionId));
        }
    }

    /**
     * Save the priceDto only in the thread transaction without storing in the {@link InMemoryPriceService#prices}
     *
     * @param priceDto
     */
    @Override
    public void save(PriceDto priceDto) {
        Transaction transaction = getThreadTransactionOrThrowExceptionIfTransactionIsNotOpen();
        transaction.getPrices().add(priceDto);
    }

    /**
     * Store the existing prices in the thread transaction into the {@link InMemoryPriceService#prices}
     * It acquire the write lock internally to prevent consumer threads to read the dirty data.
     */
    @Override
    public synchronized void commitBatchTransaction() {
        final String threadTransactionId = getThreadTransactionId();
        lock.writeLock().lock();
        logger.info(String.format("Start committing the transaction [%s] ...", threadTransactionId));

        Transaction transaction = getThreadTransactionOrThrowExceptionIfTransactionIsNotOpen();

        try {
            prices.addAll(transaction.getPrices());
            transaction.commit();

            logger.info(String.format("Transaction [%s] has been successfully committed", threadTransactionId));
        } catch (Throwable exception) {
            logger.error(String.format("Committing transaction for thread [%s] has been failed", threadTransactionId));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Remove the thread transaction without touching the {@link InMemoryPriceService#prices}
     */
    @Override
    public synchronized void rollBackBatchTransaction() {

        Transaction transaction = getThreadTransactionOrThrowExceptionIfTransactionIsNotOpen();
        transactions.remove(getThreadTransactionId());
        transaction.rollback();

        logger.info(String.format("Transaction [%s] has been rolled back", transaction.getTransactionId()));
    }

    private String getThreadTransactionId() {
        return Thread.currentThread().getName();
    }

    private boolean hasThreadAnyTransaction() {
        return transactions.containsKey(getThreadTransactionId());
    }

    /**
     * Return thread transaction if exist and open.
     *
     * @return Transaction
     * @throws TransactionException in case there would not a transaction for thread or it would not be opened.
     */
    private Transaction getThreadTransactionOrThrowExceptionIfTransactionIsNotOpen() {
        String threadTransactionId = getThreadTransactionId();
        if (transactions.containsKey(threadTransactionId)) {
            Transaction transaction = transactions.get(threadTransactionId);
            transaction.throwsExceptionIfNotActive();

            return transaction;
        }

        String message = String.format("Could not found any transaction for thread [%s]", threadTransactionId);
        logger.error(message);
        throw new TransactionException(message);
    }
}
