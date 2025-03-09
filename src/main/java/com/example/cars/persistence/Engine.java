package com.example.cars.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "engine")
@SequenceGenerator(name = "engine_seq_gen", sequenceName = "engine_seq", allocationSize = 1)
public class Engine {

    @Id
    @GeneratedValue(generator = "engine_seq_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "horse_power")
    private int horsePower;

    @Column(name = "capacity")
    private double capacity;
}
