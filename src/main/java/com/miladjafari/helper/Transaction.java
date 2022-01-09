package com.miladjafari.helper;

import com.miladjafari.excpetion.TransactionException;
import com.miladjafari.price.PriceDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction class is used to keep track of each individual thread activities for modifying the prices. if
 */
public class Transaction {

    private final String transactionId;
    private final List<PriceDto> prices = new ArrayList<>();
    private boolean isOpen = false;
    private boolean isCompleted = false;

    public Transaction(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void begin() {
        throwExceptionIfIsCompleted();
        isOpen = true;
    }

    public void commit() {
        throwsExceptionIfNotActive();
        isCompleted = true;
    }

    public void rollback() {
        throwsExceptionIfNotActive();
        isCompleted = true;
    }

    public void throwExceptionIfIsCompleted() {
        if (isCompleted) {
            throw new TransactionException("Transaction is already completed");
        }
    }
    public void throwsExceptionIfNotActive() {
        if (!isOpen) {
            throw new TransactionException("Transaction is not open");
        }
    }

    public List<PriceDto> getPrices() {
        return prices;
    }
}