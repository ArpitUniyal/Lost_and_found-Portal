package com.lostandfound.service;

import com.lostandfound.dto.CreateFoundItemRequest;
import com.lostandfound.dto.FoundItemResponse;
import com.lostandfound.entity.FoundItem;
import com.lostandfound.entity.Student;
import com.lostandfound.repository.FoundItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FoundItemService {

    private final FoundItemRepository foundItemRepository;

    public FoundItemService(FoundItemRepository foundItemRepository) {
        this.foundItemRepository = foundItemRepository;
    }

    public FoundItemResponse createFoundItem(
            CreateFoundItemRequest request,
            Student student,
            MultipartFile image
    ) throws IOException {

        FoundItem item = new FoundItem();

        item.setFinder(student);
        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setLocation(request.getLocation());
        item.setDateFound(request.getDateFound());
        item.setTimeFound(request.getTimeFound());
        item.setCategory(request.getCategory());
        item.setStatus("active");

        // Handle optional image
        if (image != null && !image.isEmpty()) {

            String originalName = image.getOriginalFilename();

            String extension = "";

            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(
                        originalName.lastIndexOf(".")
                ).toLowerCase();
            }

            if (!extension.matches("\\.(jpg|jpeg|png|webp)")) {
                throw new IllegalArgumentException(
                        "Only JPG, PNG, WEBP files allowed"
                );
            }

            if (image.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException(
                        "Image must be less than 5MB"
                );
            }

            Path uploadDir = Paths.get("public/uploads");

            Files.createDirectories(uploadDir);

            String filename =
                    "found_" +
                    System.currentTimeMillis() +
                    "_" +
                    UUID.randomUUID() +
                    extension;

            Path filePath = uploadDir.resolve(filename);

            Files.copy(image.getInputStream(), filePath);

            item.setImageUrl("/uploads/" + filename);
        }

        FoundItem savedItem = foundItemRepository.save(item);

        return toResponse(savedItem);
    }

    public List<FoundItemResponse> getAllActiveItems() {

        return foundItemRepository.findByStatus("active")
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public FoundItemResponse getItemById(Integer id) {

        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Found item not found"));

        return toResponse(item);
    }

    public void deleteFoundItem(Integer id, Student student) throws IOException {

    FoundItem item = foundItemRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Found item not found"));

    // Only the student who reported the found item can delete it
    if (!item.getFinder().getId().equals(student.getId())) {
        throw new SecurityException(
                "You are not authorized to delete this found item"
        );
    }

    // Delete the uploaded image from disk if it exists
    if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {

        Path imagePath = Paths.get(
                "public",
                item.getImageUrl().replaceFirst("^/+", "")
        );

        Files.deleteIfExists(imagePath);
    }

    // Soft delete
    item.setStatus("deleted");

    foundItemRepository.save(item);
}

    private FoundItemResponse toResponse(FoundItem item) {

        return new FoundItemResponse(
                item.getId(),
                item.getFinder().getId(),
                item.getItemName(),
                item.getDescription(),
                item.getLocation(),
                item.getDateFound(),
                item.getTimeFound(),
                item.getCategory(),
                item.getStatus(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getImageUrl(),
                item.getFinder().getName(),
                item.getFinder().getEmail(),
                item.getFinder().getPhone()
        );
    }
}