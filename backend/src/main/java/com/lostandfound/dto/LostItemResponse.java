package com.lostandfound.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class LostItemResponse {

    private Integer id;

    @JsonProperty("student_id")
    private Integer studentId;

    @JsonProperty("item_name")
    private String itemName;

    private String description;

    private String location;

    @JsonProperty("date_lost")
    private LocalDate dateLost;

    @JsonProperty("time_lost")
    private LocalTime timeLost;

    private String category;

    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("student_name")
    private String studentName;

    @JsonProperty("student_email")
    private String studentEmail;

    @JsonProperty("student_phone")
    private String studentPhone;
}