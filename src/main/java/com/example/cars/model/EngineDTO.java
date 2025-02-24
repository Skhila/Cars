package com.example.cars.model;

import lombok.*;

@Data
@AllArgsConstructor
public class EngineDTO {
    @Setter(AccessLevel.NONE)
    private final long id;

    private int horsePower;
    private double capacity;
}