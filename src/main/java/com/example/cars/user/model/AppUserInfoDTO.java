package com.example.cars.user.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
public class AppUserInfoDTO {
    @Setter(AccessLevel.NONE)
    private final Long id;

    private String username;
    private Long balanceInCents;
    private Integer ownedCarsCount;
}
