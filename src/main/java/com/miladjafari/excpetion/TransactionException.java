package com.miladjafari.excpetion;

public class TransactionException extends RuntimeException {
    public TransactionException(String message) {
        super(message);
    }
}
