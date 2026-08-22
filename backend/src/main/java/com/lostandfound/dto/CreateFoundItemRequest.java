package com.lostandfound.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CreateFoundItemRequest {

    @NotBlank(message = "Item name is required")
    @JsonProperty("item_name")
    private String itemName;

    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Please provide a valid date")
    @JsonProperty("date_found")
    private LocalDate dateFound;

    @JsonProperty("time_found")
    private LocalTime timeFound;

    private String category;
}