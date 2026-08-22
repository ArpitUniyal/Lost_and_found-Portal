package com.lostandfound.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ClaimResponse {

    private Integer id;

    @JsonProperty("lost_item_id")
    private Integer lostItemId;

    @JsonProperty("found_item_id")
    private Integer foundItemId;

    @JsonProperty("claimer_id")
    private Integer claimerId;

    private String status;

    @JsonProperty("contact_date")
    private LocalDate contactDate;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // Found item information
    @JsonProperty("found_item_name")
    private String foundItemName;

    @JsonProperty("found_location")
    private String foundLocation;

    @JsonProperty("found_status")
    private String foundStatus;

    // Lost item information
    @JsonProperty("lost_item_name")
    private String lostItemName;

    // Claimer information
    @JsonProperty("claimer_name")
    private String claimerName;

    @JsonProperty("claimer_email")
    private String claimerEmail;

    @JsonProperty("claimer_phone")
    private String claimerPhone;

    // Finder information
    @JsonProperty("finder_id")
    private Integer finderId;

    @JsonProperty("finder_name")
    private String finderName;

    @JsonProperty("finder_email")
    private String finderEmail;

    @JsonProperty("finder_phone")
    private String finderPhone;
}