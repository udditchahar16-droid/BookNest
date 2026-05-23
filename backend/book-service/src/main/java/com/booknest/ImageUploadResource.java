package com.booknest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

/**
 * Image Upload Endpoint — book-service
 *
 * POST /api/books/upload-image
 *   → file upload karo, path wapas aata hai
 *   → Admin woh path coverImageUrl mein save karta hai
 *
 * Uploaded images: backend/book-service/uploads/ folder mein save hongi
 * Angular se access: http://localhost:8082/images/filename.jpg
 */
@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class ImageUploadResource {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    /**
     * POST /api/books/upload-image
     * Multipart form-data: field name = "file"
     * Returns: { "url": "http://localhost:8082/images/abc123.jpg" }
     */
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "No file selected"));
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Only image files allowed"));
        }

        try {
            // Create uploads directory
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Unique filename — original extension rakho
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
            String fileName = UUID.randomUUID().toString() + ext;

            // Save file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL — Angular is URL ko coverImageUrl mein save karega
            String url = "http://localhost:8082/images/" + fileName;
            return ResponseEntity.ok(Map.of("url", url));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}
