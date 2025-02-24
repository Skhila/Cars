package com.example.cars.persistence;

import com.example.cars.model.EngineDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EngineRepository extends JpaRepository<Engine, Long> {

    @Query("SELECT new com.example.cars.model.EngineDTO(e.id, e.horsePower, e.capacity)" +
            "FROM Engine e WHERE e.capacity = :capacity")
    Page<EngineDTO> findEngines(double capacity, Pageable pageable);
}
