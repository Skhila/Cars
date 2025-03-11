package cars.configuration;

import com.example.cars.images.service.StorageService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public StorageService storageService() {
        StorageService mockStorageService = Mockito.mock(StorageService.class);

        when(mockStorageService.uploadImage(any(MultipartFile.class)))
                .thenReturn("https://test-bucket.s3.eu-north-1.amazonaws.com/images/test-image.jpg");

        doNothing().when(mockStorageService).deleteImage(anyString());

        return mockStorageService;
    }
}
