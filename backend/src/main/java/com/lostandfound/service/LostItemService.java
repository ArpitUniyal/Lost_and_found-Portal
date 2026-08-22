package com.lostandfound.service;

import com.lostandfound.dto.LostItemResponse;
import com.lostandfound.entity.LostItem;
import com.lostandfound.repository.LostItemRepository;
import org.springframework.stereotype.Service;
import com.lostandfound.dto.CreateLostItemRequest;
import com.lostandfound.entity.Student;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LostItemService {

    private final LostItemRepository lostItemRepository;

    public LostItemResponse createLostItem(
        CreateLostItemRequest request,
        Student student,
        MultipartFile image
) throws IOException {

    LostItem item = new LostItem();

    item.setStudent(student);
    item.setItemName(request.getItemName());
    item.setDescription(request.getDescription());
    item.setLocation(request.getLocation());
    item.setDateLost(request.getDateLost());
    item.setTimeLost(request.getTimeLost());
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
                "lost_" +
                System.currentTimeMillis() +
                "_" +
                UUID.randomUUID() +
                extension;

        Path filePath = uploadDir.resolve(filename);

        Files.copy(image.getInputStream(), filePath);

        item.setImageUrl("/uploads/" + filename);
    }

    LostItem savedItem = lostItemRepository.save(item);

    return toResponse(savedItem);
}

    public LostItemService(LostItemRepository lostItemRepository) {
        this.lostItemRepository = lostItemRepository;
    }

    public List<LostItemResponse> getAllActiveItems() {

        return lostItemRepository.findByStatus("active")
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LostItemResponse getItemById(Integer id) {

        LostItem item = lostItemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Lost item not found"));

        return toResponse(item);
    }

    public void deleteLostItem(Integer id, Student student) throws IOException {

    LostItem item = lostItemRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Lost item not found"));

    // Only the student who reported the item can delete it
    if (!item.getStudent().getId().equals(student.getId())) {
        throw new SecurityException(
                "You are not authorized to delete this lost item"
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

    lostItemRepository.save(item);
}

    private LostItemResponse toResponse(LostItem item) {

        return new LostItemResponse(
                item.getId(),
                item.getStudent().getId(),
                item.getItemName(),
                item.getDescription(),
                item.getLocation(),
                item.getDateLost(),
                item.getTimeLost(),
                item.getCategory(),
                item.getStatus(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getImageUrl(),
                item.getStudent().getName(),
                item.getStudent().getEmail(),
                item.getStudent().getPhone()
        );
    }
}