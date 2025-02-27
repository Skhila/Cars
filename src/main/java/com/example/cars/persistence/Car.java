package com.example.cars.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "car")
@SequenceGenerator(name = "engine_seq_gen", sequenceName = "engine_seq", allocationSize = 1)
public class Car {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(generator = "engine_seq_gen", strategy = GenerationType.SEQUENCE)
    private long id;

    @Column(name = "model")
    private String model;

    @Column(name = "year")
    private int year;

    @Column(name = "is_driveable")
    private boolean isDriveable;

    @ManyToOne
    @JoinColumn(name = "engine_id")
    private Engine engine;
}
