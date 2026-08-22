package com.lostandfound.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "found_items")
@Getter
@Setter
@NoArgsConstructor
public class FoundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "finder_id", nullable = false)
    private Student finder;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", nullable = false, length = 200)
    private String location;

    @Column(name = "date_found", nullable = false)
    private LocalDate dateFound;

    @Column(name = "time_found")
    private LocalTime timeFound;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "image_url", length = 255)
    private String imageUrl;
}