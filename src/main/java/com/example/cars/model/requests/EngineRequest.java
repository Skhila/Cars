package com.example.cars.model.requests;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EngineRequest {
    @Positive
    private int horsePower;

    @Positive
    private double capacity;
}
