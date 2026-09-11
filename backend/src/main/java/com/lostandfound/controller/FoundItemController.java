package com.lostandfound.controller;

import com.lostandfound.dto.CreateFoundItemRequest;
import com.lostandfound.dto.FoundItemResponse;
import com.lostandfound.entity.Student;
import com.lostandfound.service.FoundItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/found-items")
public class FoundItemController {

    private final FoundItemService foundItemService;

    public FoundItemController(FoundItemService foundItemService) {
        this.foundItemService = foundItemService;
    }

    @GetMapping
    public ResponseEntity<?> getAllFoundItems() {

        try {
            List<FoundItemResponse> items =
                    foundItemService.getAllActiveItems();

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", Map.of(
                                    "items", items
                            )
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to fetch found items"
                    ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFoundItem(
            @PathVariable Integer id
    ) {

        try {
            FoundItemResponse item =
                    foundItemService.getItemById(id);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", Map.of(
                                    "item", item
                            )
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", "Found item not found"
                    ));
        }
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createFoundItem(
            @RequestParam("item_name") String itemName,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("date_found") String dateFound,
            @RequestParam(value = "time_found", required = false) String timeFound,
            @RequestParam(value = "category", required = false) String category,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication
    ) {

        try {
            if (itemName == null || itemName.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "Item name is required"
                        )
                );
            }

            if (description == null || description.length() < 10) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "Description must be at least 10 characters"
                        )
                );
            }

            if (location == null || location.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "Location is required"
                        )
                );
            }

            LocalDate parsedDate = LocalDate.parse(dateFound);

            LocalTime parsedTime = null;

            if (timeFound != null && !timeFound.isBlank()) {
                parsedTime = LocalTime.parse(timeFound);
            }

            CreateFoundItemRequest request =
                    new CreateFoundItemRequest();

            request.setItemName(itemName);
            request.setDescription(description);
            request.setLocation(location);
            request.setDateFound(parsedDate);
            request.setTimeFound(parsedTime);
            request.setCategory(category);

            Student student =
                    (Student) authentication.getPrincipal();

            FoundItemResponse item =
                    foundItemService.createFoundItem(
                            request,
                            student,
                            image
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Found item reported successfully",
                            "data", Map.of(
                                    "item", item
                            )
                    )
            );

        } catch (java.time.format.DateTimeParseException e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", "Please provide a valid date/time"
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", "Failed to report found item"
                            )
                    );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFoundItem(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        try {
            Student student =
                    (Student) authentication.getPrincipal();

            foundItemService.deleteFoundItem(id, student);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Found item deleted successfully"
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", "Found item not found"
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", "Failed to delete found item"
                            )
                    );
        }
    }
}