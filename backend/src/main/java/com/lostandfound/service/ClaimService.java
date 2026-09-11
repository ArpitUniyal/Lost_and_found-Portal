package com.lostandfound.service;

import com.lostandfound.dto.ClaimResponse;
import com.lostandfound.dto.NotifyOwnerRequest;
import com.lostandfound.dto.VerifyClaimRequest;
import com.lostandfound.entity.Claim;
import com.lostandfound.entity.FoundItem;
import com.lostandfound.entity.LostItem;
import com.lostandfound.entity.Student;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.repository.FoundItemRepository;
import com.lostandfound.repository.LostItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    private static final Set<String> LOCKED_STATUSES = Set.of(
            "approved",
            "claimer_marked",
            "finder_marked",
            "pending",
            "completed"
    );

    private final ClaimRepository claimRepository;
    private final FoundItemRepository foundItemRepository;   //service dependencies
    private final LostItemRepository lostItemRepository;
    private final EmailService emailService;

    public ClaimService(
            ClaimRepository claimRepository,
            FoundItemRepository foundItemRepository,
            LostItemRepository lostItemRepository,
            EmailService emailService
    ) {
        this.claimRepository = claimRepository;
        this.foundItemRepository = foundItemRepository;
        this.lostItemRepository = lostItemRepository;
        this.emailService = emailService;
    }

    @Transactional //Execute the database-related work inside this method as one transaction.
    public ClaimResponse verifyClaim(
            VerifyClaimRequest request,  //This is a DTO containing data sent by the client.
            Student claimer  //This is the currently authenticated student who is trying to claim the found item.
    ) {

        if (request == null || request.getFoundItemId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,  //Stop executing this method and return an HTTP 400 Bad Request error.
                    "found_item_id is required and must be an integer"
            );
        }

        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "location is required"
            );
        }

        FoundItem foundItem = foundItemRepository
                .findById(request.getFoundItemId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Found item not found"
                        )
                );

        if (!"active".equals(foundItem.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Item is not available for claims"
            );
        }

        if (foundItem.getFinder() != null
                && foundItem.getFinder().getId().equals(claimer.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot claim your own found item"
            );
        }

        List<Claim> existingClaims =
                claimRepository.findByFoundItemIdAndStatusIn( 
                        foundItem.getId(),//find all claims for this found item that are in one of the LOCKED_STATUSES.
                        LOCKED_STATUSES
                );

        boolean lockedByAnotherUser = existingClaims.stream()
        .anyMatch(claim ->
                claim.getClaimer() != null
                        && !claim.getClaimer().getId().equals(claimer.getId())//The claimer of this existing claim is not the current user.
        );

if (lockedByAnotherUser) {
    throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "This item is already requested by another user"
    );
}

boolean alreadyClaimedByCurrentUser = existingClaims.stream()
        .anyMatch(claim ->
                claim.getClaimer() != null
                        && claim.getClaimer().getId().equals(claimer.getId())
        );

if (alreadyClaimedByCurrentUser) {
    throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "You already have an active claim for this item"
    );
}

        String providedLocation = normalizeLocation(
                request.getLocation()
        );

        String storedLocation = normalizeLocation(
                foundItem.getLocation()
        );

        boolean matched =
                !providedLocation.isEmpty()
                        && !storedLocation.isEmpty()
                        && providedLocation.equals(storedLocation);

        
        if (!matched) {

            LocalDate today = LocalDate.now(); //Get today's date example: 2026-09-10

            LocalDateTime startOfDay =    //Get the beginning of today
                    today.atStartOfDay(); //Example : 2026-09-10 00:00:00

            LocalDateTime startOfNextDay =
                    today.plusDays(1).atStartOfDay(); //Get the beginning of tomorrow
                                                               //Example : 2026-09-11 00:00:00
            long rejectedCount =
                    claimRepository
                            .countByFoundItemIdAndClaimerIdAndStatusAndCreatedAtBetween(
                                    foundItem.getId(), //Count claims where the found item is this item, 
                                    claimer.getId(),  //the claimer is this student
                                    "rejected",//the status is rejected 
                                    startOfDay,   //and the creation time falls within today's range.
                                    startOfNextDay
                            );

            if (rejectedCount >= 2) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Maximum daily claim attempts (3) reached for this item"
                );
            }
        }

        Claim claim = new Claim();

        claim.setLostItem(null);
        claim.setFoundItem(foundItem);
        claim.setClaimer(claimer);
        claim.setStatus(matched ? "approved" : "rejected");

        Claim savedClaim = claimRepository.save(claim);


        if ("approved".equals(savedClaim.getStatus())
        && savedClaim.getFoundItem() != null
        && savedClaim.getFoundItem().getFinder() != null) {

    Student finder = savedClaim.getFoundItem().getFinder();

    emailService.sendClaimApprovedNotification(
            finder.getEmail(),
            finder.getName(),
            savedClaim.getFoundItem().getItemName(),
            savedClaim.getFoundItem().getLocation()
    );
}

return toResponse(savedClaim);

    }


    
    @Transactional
    public ClaimResponse notifyOwner(
            NotifyOwnerRequest request,
            Student finder
    ) {

        if (request == null || request.getLostItemId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "lost_item_id is required and must be an integer"
            );
        }

        LostItem lostItem = lostItemRepository
                .findById(request.getLostItemId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Lost item not found"
                        )
                );

        List<Claim> existingLostClaims =
                claimRepository.findByLostItemIdAndStatusIn(
                        lostItem.getId(),
                        LOCKED_STATUSES
                );

        if (!existingLostClaims.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This lost item has already been notified by another user"
            );
        }

        FoundItem foundItem;

        if (request.getFoundItemId() != null) {

            foundItem = foundItemRepository
                    .findById(request.getFoundItemId())
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Found item not found"
                            )
                    );

            if (foundItem.getFinder() == null
                    || !foundItem.getFinder().getId().equals(finder.getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only link your own found item"
                );
            }

        } else {

            
            foundItem = new FoundItem();

            foundItem.setFinder(finder);
            foundItem.setItemName(
                    lostItem.getItemName() != null
                            ? lostItem.getItemName()
                            : "Found item"
            );
            foundItem.setDescription(
                    lostItem.getDescription() != null
                            ? lostItem.getDescription()
                            : ""
            );
            foundItem.setLocation(
                    lostItem.getLocation() != null
                            ? lostItem.getLocation()
                            : ""
            );
            foundItem.setDateFound(LocalDate.now());
            foundItem.setStatus("active");

            foundItem = foundItemRepository.save(foundItem);
        }

       
        List<Claim> existingPairClaims =
                claimRepository.findByFoundItemIdAndStatusIn(
                        foundItem.getId(),
                        LOCKED_STATUSES
                );

        boolean duplicatePair = existingPairClaims.stream()
                .anyMatch(claim ->
                        claim.getLostItem() != null
                                && claim.getLostItem().getId().equals(lostItem.getId())
                );

        if (duplicatePair) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A notification already exists for this lost item"
            );
        }

        Claim claim = new Claim();

        claim.setLostItem(lostItem);
        claim.setFoundItem(foundItem);
        claim.setClaimer(lostItem.getStudent());
        claim.setStatus("approved");

        Claim savedClaim = claimRepository.save(claim);

        Student owner = lostItem.getStudent();

if (owner != null) {
    emailService.sendOwnerNotification(
            owner.getEmail(),
            owner.getName(),
            lostItem.getItemName()
    );
}

        return toResponse(savedClaim);
    }


    @Transactional(readOnly = true)  //Execute this method inside a transaction, and this method is intended only for reading data.
    public List<ClaimResponse> getFinderPendingClaims(Student finder) {
//This method is used to retrieve all pending/active claims
//that belong to found items reported by a particular
//then sort them newest-first and convert them into response DTOs.

        List<Claim> claims =
                claimRepository.findByFoundItemFinderIdAndStatusIn(
                        finder.getId(),
                        LOCKED_STATUSES
                );

        return claims.stream()
                .filter(claim ->
        claim.getFoundItem() != null
                && "active".equals(claim.getFoundItem().getStatus())

                )
                .sorted((a, b) -> {

                    LocalDateTime aDate = a.getCreatedAt();
                    LocalDateTime bDate = b.getCreatedAt();

                    if (aDate == null && bDate == null) {
                        return 0;
                    }

                    if (aDate == null) {
                        return 1;
                    }

                    if (bDate == null) {
                        return -1;
                    }

                    return bDate.compareTo(aDate);
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    /**
     * Returns all claims belonging to the logged-in student.
     */
    @Transactional(readOnly = true)
    public List<ClaimResponse> getMyClaims(Student claimer) {

        return claimRepository
                .findByClaimerIdOrderByCreatedAtDesc(claimer.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


      /**
 * This method is used to find all Found Item IDs that should be considered “locked” and 
 * therefore should not be available for another normal claim.
 */
    @Transactional(readOnly = true)
    public List<Integer> getLockedFoundItemIds() {////Which Found Items are already locked/unavailable?

        Set<Integer> ids = new HashSet<>();

        for (Claim claim :
                claimRepository.findByStatus("approved")) {

            if (claim.getFoundItem() != null) {
                ids.add(claim.getFoundItem().getId());
            }
        }

        for (String status :
                List.of(   //These are the statuses that indicate a claim is locked.
                        "claimer_marked",
                        "finder_marked",
                        "pending",
                        "completed"
                )) {

            for (Claim claim :
                    claimRepository.findByStatus(status)) { //find all claims that are in one of the LOCKED_STATUSES.

                if (claim.getFoundItem() != null) {
                    ids.add(claim.getFoundItem().getId());
                }
            }
        }

        for (FoundItem item :
                foundItemRepository.findByStatus("closed")) {

            ids.add(item.getId());
        }

        return new ArrayList<>(ids);
    }

  
@Transactional(readOnly = true)
public List<Integer> getHiddenNotificationFoundItemIds() { //Which Found Items should have the “Notify Owner” option hidden?

    Set<Integer> ids = new HashSet<>();

    for (String status : LOCKED_STATUSES) {

        for (Claim claim : claimRepository.findByStatus(status)) {

            if (claim.getFoundItem() != null
                    && claim.getLostItem() != null) {

                ids.add(claim.getFoundItem().getId());
            }
        }
    }

    return new ArrayList<>(ids);
}

//Find all Lost Item IDs that are already involved in a locked claim
// and return those IDs so the application can treat those lost items as unavailable for 
// another claim/notification action.
    @Transactional(readOnly = true)
    public List<Integer> getLockedLostItemIds() { 

        Set<Integer> ids = new HashSet<>();

        for (String status :
                LOCKED_STATUSES) {

            for (Claim claim :
                    claimRepository.findByStatus(status)) {

                if (claim.getLostItem() != null) {
                    ids.add(claim.getLostItem().getId());
                }
            }
        }

        return new ArrayList<>(ids);
    }


    /**
     * Claimer confirms handover.
     */
    @Transactional
    public String claimerConfirm(
            Integer claimId,
            Student claimer
    ) {

        Claim claim = getClaimOrThrow(claimId);

        if (claim.getClaimer() == null
                || !claim.getClaimer().getId().equals(claimer.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Not authorized to confirm this claim"
            );
        }

        if (!"approved".equals(claim.getStatus())
                && !"finder_marked".equals(claim.getStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Claim is not in a confirmable state"
            );
        }

        if ("finder_marked".equals(claim.getStatus())) {

            completeClaim(claim);

            return "Both confirmed. Claim completed and item closed.";
        }

        claim.setStatus("claimer_marked");
        claimRepository.save(claim);

        return "Claimer confirmation recorded";
    }


    /**
     * Finder confirms that the item was returned.
     */
    @Transactional
    public String finderReturned(
            Integer claimId,
            Student finder
    ) {

        Claim claim = getClaimOrThrow(claimId);

        FoundItem foundItem = claim.getFoundItem();

        if (foundItem == null
                || foundItem.getFinder() == null
                || !foundItem.getFinder().getId().equals(finder.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Not authorized to mark return"
            );
        }

        if (!"approved".equals(claim.getStatus())
                && !"claimer_marked".equals(claim.getStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Claim is not in a returnable state"
            );
        }

        if ("claimer_marked".equals(claim.getStatus())) {

            completeClaim(claim);

            return "Both confirmed. Claim completed and item closed.";
        }

        claim.setStatus("finder_marked");
        claimRepository.save(claim);

        return "Finder return recorded";
    }


    private void completeClaim(Claim claim) {

        claim.setStatus("completed");
        claimRepository.save(claim);

        FoundItem foundItem = claim.getFoundItem();

        if (foundItem != null) {

            foundItem.setStatus("closed");
            foundItemRepository.save(foundItem);

            List<Claim> otherClaims =
                    claimRepository.findByFoundItemId(
                            foundItem.getId()
                    );

            for (Claim other : otherClaims) {

                if (!other.getId().equals(claim.getId())) {
                    other.setStatus("completed");
                    claimRepository.save(other);
                }
            }
        }

        /*
         * If this claim is linked to a lost item,
         * mark that lost item completed.
         */
        if (claim.getLostItem() != null) {

            LostItem lostItem = claim.getLostItem();
            lostItem.setStatus("completed");
            lostItemRepository.save(lostItem);
        }
    }


    private Claim getClaimOrThrow(Integer claimId) {

        if (claimId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Claim ID is required"
            );
        }

        return claimRepository
                .findById(claimId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Claim not found"
                        )
                );
    }


    private String normalizeLocation(String value) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }


    private ClaimResponse toResponse(Claim claim) {

        ClaimResponse response = new ClaimResponse();

        response.setId(claim.getId());
        response.setStatus(claim.getStatus());
        response.setContactDate(claim.getContactDate());
        response.setCreatedAt(claim.getCreatedAt());
        response.setUpdatedAt(claim.getUpdatedAt());

        if (claim.getLostItem() != null) {

            LostItem lostItem = claim.getLostItem();

            response.setLostItemId(lostItem.getId());
            response.setLostItemName(lostItem.getItemName());
        }

        if (claim.getFoundItem() != null) {

            FoundItem foundItem = claim.getFoundItem();

            response.setFoundItemId(foundItem.getId());
            response.setFoundItemName(foundItem.getItemName());
            response.setFoundLocation(foundItem.getLocation());
            response.setFoundStatus(foundItem.getStatus());

            if (foundItem.getFinder() != null) {

                Student finder = foundItem.getFinder();

                response.setFinderId(finder.getId());
                response.setFinderName(finder.getName());
                response.setFinderEmail(finder.getEmail());
                response.setFinderPhone(finder.getPhone());
            }
        }

        if (claim.getClaimer() != null) {

            Student claimer = claim.getClaimer();

            response.setClaimerId(claimer.getId());
            response.setClaimerName(claimer.getName());
            response.setClaimerEmail(claimer.getEmail());
            response.setClaimerPhone(claimer.getPhone());
        }

        return response;
    }
}