package com.example.cars.images.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    private final List<String> VALID_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"
    );

    public String uploadImage(MultipartFile file) {
        validateFileExtension(file);
        String filename = generateFileName(file);

        try {
            File convertedFile = convertMultiPartToFile(file);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filename, convertedFile);

            s3Client.putObject(putObjectRequest);
            convertedFile.delete();

            log.info(generatePublicUrl(filename));
            return generatePublicUrl(filename);

        } catch (IOException ioException) {
            log.error("Error converting multipart file", ioException);
            throw new RuntimeException("Error uploading file to S3", ioException);
        }
    }

    public void deleteImage(String imageUrl) {
        String fileName = extractObjectKeyFromUrl(imageUrl);

        if (fileName != null) {
            s3Client.deleteObject(bucketName, fileName);
            log.info("Deleted image: {}", fileName);
        }
    }

    private String extractObjectKeyFromUrl(String imageUrl) {
        if (imageUrl != null && imageUrl.contains("/")) {
            return imageUrl.substring(imageUrl.lastIndexOf("/images") + 1);
        }
        return null;
    }

    private void validateFileExtension(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!VALID_IMAGE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("File type not supported, Supported types are: " + VALID_IMAGE_TYPES);
        }
    }

    private String generatePublicUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, s3Client.getRegion(), fileName);
    }

    private String generateFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        String imageName = "";

        if (!originalFilename.contains(".")) {
            throw new IllegalArgumentException("File should have extension: " + extension);
        } else {
            imageName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return "images/" + UUID.randomUUID() + "_" + imageName + extension;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }
}
