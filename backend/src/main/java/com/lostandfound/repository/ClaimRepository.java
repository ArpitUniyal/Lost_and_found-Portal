package com.lostandfound.repository;

import com.lostandfound.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Integer> {

    List<Claim> findByClaimerIdOrderByCreatedAtDesc(
            Integer claimerId
    );

    List<Claim> findByFoundItem_Finder_IdOrderByCreatedAtDesc(
            Integer finderId
    );

    List<Claim> findByFoundItemId(
            Integer foundItemId
    );

    List<Claim> findByLostItemId(
            Integer lostItemId
    );

    List<Claim> findByLostItemIdAndStatusIn(
            Integer lostItemId,
            Collection<String> statuses
    );

    List<Claim> findByStatus(
            String status
    );

    List<Claim> findByFoundItemIdAndStatusIn(
            Integer foundItemId,
            Collection<String> statuses
    );

    List<Claim> findByClaimerId(
            Integer claimerId
    );

    List<Claim> findByFoundItemFinderIdAndStatusIn(
            Integer finderId,
            Collection<String> statuses
    );

    List<Claim> findByLostItemStudentIdAndStatusIn(
            Integer studentId,
            Collection<String> statuses
    );

    long countByFoundItemIdAndClaimerIdAndStatusAndCreatedAtBetween(
            Integer foundItemId,
            Integer claimerId,
            String status,
            LocalDateTime start,
            LocalDateTime end
    );
}