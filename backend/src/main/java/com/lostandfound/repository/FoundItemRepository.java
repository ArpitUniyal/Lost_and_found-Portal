package com.lostandfound.repository;

import com.lostandfound.entity.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoundItemRepository extends JpaRepository<FoundItem, Integer> {

    List<FoundItem> findByFinderId(Integer finderId);

    List<FoundItem> findByStatus(String status);
}