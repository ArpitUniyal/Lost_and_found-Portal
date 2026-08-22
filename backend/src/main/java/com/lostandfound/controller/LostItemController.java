package com.lostandfound.controller;

import com.lostandfound.dto.CreateLostItemRequest;
import com.lostandfound.dto.LostItemResponse;
import com.lostandfound.entity.Student;
import com.lostandfound.service.LostItemService;
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
@RequestMapping("/api/lost-items")
public class LostItemController {

    private final LostItemService lostItemService;

    public LostItemController(LostItemService lostItemService) {
        this.lostItemService = lostItemService;
    }

    @GetMapping
    public ResponseEntity<?> getAllLostItems() {

        try {
            List<LostItemResponse> items =
                    lostItemService.getAllActiveItems();

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
                            "message", "Failed to fetch lost items"
                    ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLostItem(
            @PathVariable Integer id
    ) {
        try {
            
            LostItemResponse item =
                    lostItemService.getItemById(id);

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
                            "message", "Lost item not found"
                    ));
        }
    }

    @PostMapping(consumes = "multipart/form-data")
public ResponseEntity<?> createLostItem(
        @RequestParam("item_name") String itemName,
        @RequestParam("description") String description,
        @RequestParam("location") String location,
        @RequestParam("date_lost") String dateLost,
        @RequestParam(value = "time_lost", required = false) String timeLost,
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

        LocalDate parsedDate = LocalDate.parse(dateLost);

        LocalTime parsedTime = null;

        if (timeLost != null && !timeLost.isBlank()) {
            parsedTime = LocalTime.parse(timeLost);
        }
         
        CreateLostItemRequest request = new CreateLostItemRequest();

        request.setItemName(itemName);
        request.setDescription(description);
        request.setLocation(location);
        request.setDateLost(parsedDate);
        request.setTimeLost(parsedTime);
        request.setCategory(category);

        Student student = (Student) authentication.getPrincipal();
        
        LostItemResponse item =
                lostItemService.createLostItem(
                        request,
                        student,
                        image
                );

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Lost item reported successfully",
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
                                "message", "Failed to report lost item"
                        )
                );
    }
}
@DeleteMapping("/{id}")
public ResponseEntity<?> deleteLostItem(
        @PathVariable Integer id,
        Authentication authentication
) {

    try {
        Student student = (Student) authentication.getPrincipal();

        lostItemService.deleteLostItem(id, student);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Lost item deleted successfully"
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
                                "message", "Lost item not found"
                        )
                );

    } catch (Exception e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        Map.of(
                                "success", false,
                                "message", "Failed to delete lost item"
                        )
                );
    }
}
}