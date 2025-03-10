package com.example.cars.error;

public class CarAlreadyPurchasedException extends RuntimeException {
    public CarAlreadyPurchasedException(String message) {
        super(message);
    }
}
