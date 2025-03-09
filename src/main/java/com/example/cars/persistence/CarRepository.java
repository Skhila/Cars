package com.example.cars.persistence;

import com.example.cars.model.CarDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CarRepository extends JpaRepository<Car, Long> {

    @Query(
            "SELECT NEW com.example.cars.model.CarDTO" +
                    "(c.id, c.model, c.year, c.isDriveable, c.priceInCents, " +
                    "NEW com.example.cars.model.EngineDTO(e.id, e.horsePower, e.capacity), " +
                    "c.imageUrl, c.salesCount) " +
                    "FROM Car c JOIN c.engine e"
    )
    Page<CarDTO> findCars(Pageable pageable);
}
