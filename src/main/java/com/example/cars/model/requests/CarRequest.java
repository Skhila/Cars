package com.example.cars.model.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarRequest {
    @NotBlank
    @Size(min = 1, max = 255)
    private String model;

    @Min(1940)
    private int year;

    private boolean isDriveable;

    @PositiveOrZero
    private Long priceInCents;

    @Positive
    private Long engineId;
}
