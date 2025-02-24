package com.example.cars.model;

import lombok.*;

@Data
@AllArgsConstructor
public class CarDTO {
    @Setter(AccessLevel.NONE)
    private final long id;

    private String model;
    private int year;
    private boolean driveable;
    private EngineDTO engine;
}
