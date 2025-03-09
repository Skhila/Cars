package com.example.cars.images.controller;

import com.example.cars.images.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import static com.example.cars.security.AuthorizationConstants.ADMIN;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/images")
public class StorageController {

    @Autowired
    private StorageService storageService;

    @PreAuthorize(ADMIN)
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("image") MultipartFile image) {
        String imageUrl = storageService.uploadImage(image);

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        response.put("message", "Image uploaded successfully");

        return ResponseEntity.ok(response);
    }

    @PreAuthorize(ADMIN)
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImage(@RequestParam(name = "url") String imageUrl) {
        storageService.deleteImage(imageUrl);
        return ResponseEntity.ok().body("Image deleted successfully");
    }

}
