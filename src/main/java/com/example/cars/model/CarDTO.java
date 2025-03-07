package com.example.cars.model;

import lombok.*;

@Data
@AllArgsConstructor
public class CarDTO {
    @Setter(AccessLevel.NONE)
    private final Long id;

    private String model;
    private int year;
    private boolean driveable;
    private Long priceInCents;
    private EngineDTO engine;
}
