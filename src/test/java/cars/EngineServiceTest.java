package cars;

import com.example.cars.model.EngineDTO;
import com.example.cars.model.requests.EngineRequest;
import com.example.cars.persistence.Engine;
import com.example.cars.persistence.EngineRepository;
import com.example.cars.services.EngineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EngineServiceTest {

    @Mock
    private EngineRepository engineRepository;

    @InjectMocks
    private EngineService engineService;

    @Test
    void testGetEngines() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<EngineDTO> enginePage = new PageImpl<EngineDTO>(List.of(new EngineDTO(1L, 150, 2.0)));
        when(engineRepository.findEngines(2.0, pageRequest)).thenReturn(enginePage);

        // when
        Page<EngineDTO> result = engineService.getEngines(0, 10, 2.0);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(2.0, result.getContent().getFirst().getCapacity());
        verify(engineRepository).findEngines(2.0, pageRequest);
    }

    @Test
    void testCreateEngine() {
        // Given
        EngineRequest engineRequest = new EngineRequest(180, 2.0);
        when(engineRepository.save(any(Engine.class))).thenReturn(buildEngine());

        // When
        engineService.createEngine(engineRequest);

        // Then
        verify(engineRepository).save(any(Engine.class));
    }

    @Test
    void testUpdateEngine() {
        // Given
        EngineRequest engineRequest = new EngineRequest(200, 2.0);
        when(engineRepository.findById(1L)).thenReturn(Optional.of(buildEngine()));
        when(engineRepository.save(any(Engine.class))).thenReturn(buildEngine());

        // When
        EngineDTO result = engineService.updateEngine(1L, engineRequest);

        // Then
        assertEquals(2.0, result.getCapacity());
        assertEquals(200, result.getHorsePower());
        verify(engineRepository).findById(1L);
        verify(engineRepository).save(any(Engine.class));
    }

    @Test
    void testDeleteEngine() {
        engineService.deleteEngine(1L);

        verify(engineRepository).deleteById(1L);
    }

    @Test
    void testFindEngine() {
        // Given
        when(engineRepository.findById(1L)).thenReturn(Optional.of(buildEngine()));

        // When
        Engine result = engineService.findEngineById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(engineRepository).findById(1L);
    }

    private Engine buildEngine() {
        Engine engine = new Engine();
        engine.setId(1L);
        engine.setCapacity(2.0);
        engine.setHorsePower(180);
        return engine;
    }

}
