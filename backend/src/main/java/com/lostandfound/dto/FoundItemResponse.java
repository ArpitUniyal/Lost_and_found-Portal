package com.lostandfound.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class FoundItemResponse {

    private Integer id;

    @JsonProperty("finder_id")
    private Integer finderId;

    @JsonProperty("item_name")
    private String itemName;

    private String description;

    private String location;

    @JsonProperty("date_found")
    private LocalDate dateFound;

    @JsonProperty("time_found")
    private LocalTime timeFound;

    private String category;

    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("finder_name")
    private String finderName;

    @JsonProperty("finder_email")
    private String finderEmail;

    @JsonProperty("finder_phone")
    private String finderPhone;
}