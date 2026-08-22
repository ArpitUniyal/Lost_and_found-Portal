package com.lostandfound.controller;

import com.lostandfound.dto.ClaimResponse;
import com.lostandfound.dto.NotifyOwnerRequest;
import com.lostandfound.dto.VerifyClaimRequest;
import com.lostandfound.entity.Student;
import com.lostandfound.service.ClaimService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }


    // =========================================================
    // VERIFY CLAIM
    // POST /api/claims/verify-request
    // =========================================================

    @PostMapping("/verify-request")
    public ResponseEntity<?> verifyClaim(
            @RequestBody VerifyClaimRequest request,
            Authentication authentication
    ) {

        try {

            Student student =
                    (Student) authentication.getPrincipal();

            ClaimResponse claim =
                    claimService.verifyClaim(
                            request,
                            student
                    );

            if ("approved".equals(claim.getStatus())) {
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                    Map.of(
                            "success", true,
                            "message", "Claim approved",
                            "data", Map.of(
                                    "claim", claim
                            )
                    )
            );
}

return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(
                Map.of(
                        "success", false,
                        "message", "Verification failed. Claim rejected",
                        "data", Map.of(
                                "claim", claim
                        )
                )
        );

        } catch (org.springframework.web.server.ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", e.getReason() != null
                                            ? e.getReason()
                                            : "Request failed"
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", "Failed to verify claim"
                            )
                    );
        }
    }


    // =========================================================
    // NOTIFY OWNER
    // POST /api/claims/notify-owner
    // =========================================================

    @PostMapping("/notify-owner")
    public ResponseEntity<?> notifyOwner(
            @RequestBody NotifyOwnerRequest request,
            Authentication authentication
    ) {

        try {

            Student student =
                    (Student) authentication.getPrincipal();

            ClaimResponse claim =
                    claimService.notifyOwner(
                            request,
                            student
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Owner notified",
                            "data", Map.of(
                                    "claim", claim
                            )
                    )
            );

        } catch (org.springframework.web.server.ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", e.getReason() != null
                                            ? e.getReason()
                                            : "Request failed"
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", "Failed to notify owner"
                            )
                    );
        }
    }


    // =========================================================
    // FINDER PENDING CLAIMS
    // GET /api/claims/finder/pending
    // =========================================================

    @GetMapping("/finder/pending")
    public ResponseEntity<?> getFinderPendingClaims(
            Authentication authentication
    ) {

        try {

            Student student =
                    (Student) authentication.getPrincipal();

            List<ClaimResponse> claims =
                    claimService.getFinderPendingClaims(student);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", Map.of(
                                    "claims", claims
                            )
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", "Failed to fetch pending claims"
                            )
                    );
        }
    }


    // =========================================================
    // MY CLAIMS
    // GET /api/claims/my
    // =========================================================

    @GetMapping("/my")
    public ResponseEntity<?> getMyClaims(
            Authentication authentication
    ) {

        try {

            Student student =
                    (Student) authentication.getPrincipal();

            List<ClaimResponse> claims =
                    claimService.getMyClaims(student);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", Map.of(
                                    "claims", claims
                            )
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", "Failed to fetch claims"
                            )
                    );
        }
    }


    // =========================================================
    // LOCKED FOUND ITEMS
    // GET /api/claims/found/locked
    // =========================================================

    @GetMapping("/found/locked")
    public ResponseEntity<?> getLockedFoundItems() {

        try {

            List<Integer> foundItemIds =
                    claimService.getLockedFoundItemIds();

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", Map.of(
                                    "found_item_ids", foundItemIds
                            )
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", "Failed to fetch locked found items"
                            )
                    );
        }
    }

    @GetMapping("/found/notification-hidden")
public ResponseEntity<?> getNotificationHiddenFoundItems() {
    try {
        List<Integer> foundItemIds =
                claimService.getHiddenNotificationFoundItemIds();

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "data", Map.of(
                                "found_item_ids", foundItemIds
                        )
                )
        );

    } catch (Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        Map.of(
                                "success", false,
                                "message", "Failed to fetch notification-hidden found items"
                        )
                );
    }
}


    // =========================================================
    // LOCKED LOST ITEMS
    // GET /api/claims/lost/locked
    // =========================================================

    @GetMapping("/lost/locked")
    public ResponseEntity<?> getLockedLostItems() {

        try {

            List<Integer> lostItemIds =
                    claimService.getLockedLostItemIds();

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", Map.of(
                                    "lost_item_ids", lostItemIds
                            )
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", "Failed to fetch locked lost items"
                            )
                    );
        }
    }


    // =========================================================
    // CLAIMER CONFIRM
    // PATCH /api/claims/{id}/claimer-confirm
    // =========================================================

    @PatchMapping("/{id}/claimer-confirm")
    public ResponseEntity<?> claimerConfirm(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        try {

            Student student =
                    (Student) authentication.getPrincipal();

            String message =
                    claimService.claimerConfirm(
                            id,
                            student
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", message
                    )
            );

        } catch (org.springframework.web.server.ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", e.getReason() != null
                                            ? e.getReason()
                                            : "Request failed"
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", "Failed to confirm claim"
                            )
                    );
        }
    }


    // =========================================================
    // FINDER RETURNED
    // PATCH /api/claims/{id}/finder-returned
    // =========================================================

    @PatchMapping("/{id}/finder-returned")
    public ResponseEntity<?> finderReturned(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        try {

            Student student =
                    (Student) authentication.getPrincipal();

            String message =
                    claimService.finderReturned(
                            id,
                            student
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", message
                    )
            );

        } catch (org.springframework.web.server.ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", e.getReason() != null
                                            ? e.getReason()
                                            : "Request failed"
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", "Failed to mark return"
                            )
                    );
        }
    }
}