package com.example.cars.user.model;

import com.example.cars.model.CarDTO;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.util.Set;

@Data
@AllArgsConstructor
public class AppUserDTO {
    @Setter(AccessLevel.NONE)
    private final Long id;

    private String username;
    private Long balanceInCents;
    private Set<CarDTO> cars;
}
