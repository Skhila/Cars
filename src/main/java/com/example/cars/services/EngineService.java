package com.example.cars.services;

import com.example.cars.error.NotFoundException;
import com.example.cars.model.EngineDTO;
import com.example.cars.model.requests.EngineRequest;
import com.example.cars.persistence.Engine;
import com.example.cars.persistence.EngineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EngineService {
    private final EngineRepository engineRepository;

    public Page<EngineDTO> getEngines(int page, int pageSize, double capacity) {
        return engineRepository.findEngines(capacity, PageRequest.of(page, pageSize));
    }

    public void createEngine(EngineRequest request) {
        Engine engine = new Engine();
        engine.setCapacity(request.getCapacity());
        engine.setHorsePower(request.getHorsePower());

        engineRepository.save(engine);
    }

    public EngineDTO updateEngine(Long id, EngineRequest request) {
        Engine engine = engineRepository.findById(id).orElseThrow(() ->  buildNotFoundException(id));

        engine.setHorsePower(request.getHorsePower());
        engine.setCapacity(request.getCapacity());

        engineRepository.save(engine);

        return mapEngine(engine);
    }

    public void deleteEngine(Long id) {
        engineRepository.deleteById(id);
    }

    public Engine findEngineById(long id) {
        return engineRepository.findById(id).orElseThrow(() ->  buildNotFoundException(id));
    }

    private EngineDTO mapEngine(Engine engine) {
        return new EngineDTO(
                engine.getId(),
                engine.getHorsePower(),
                engine.getCapacity()
        );
    }

    private NotFoundException buildNotFoundException(Long id) {
        return new NotFoundException("Engine with id " + id + " not found");
    }
}
