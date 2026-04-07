package com.example.currencyexchange.exception;

/**
 * Thrown when an operation requires an active currency but the record is inactive.
 */
public class InactiveCurrencyException extends RuntimeException {
    public InactiveCurrencyException(String code) {
        super("Currency '" + code + "' is inactive");
    }
}
