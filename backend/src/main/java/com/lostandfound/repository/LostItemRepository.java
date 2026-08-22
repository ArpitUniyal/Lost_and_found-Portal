package com.lostandfound.repository;

import com.lostandfound.entity.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LostItemRepository extends JpaRepository<LostItem, Integer> {

    List<LostItem> findByStudentId(Integer studentId);

    List<LostItem> findByStatus(String status);
}