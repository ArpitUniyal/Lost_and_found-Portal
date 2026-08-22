package com.lostandfound.controller;

import com.lostandfound.dto.AuthResponse;
import com.lostandfound.dto.LoginRequest;
import com.lostandfound.dto.RegisterRequest;
import com.lostandfound.dto.ForgotPasswordRequest;
import com.lostandfound.dto.ResetPasswordRequest;
import com.lostandfound.dto.UserResponse;
import com.lostandfound.entity.Student;
import org.springframework.security.core.Authentication;
import com.lostandfound.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        try {
            AuthResponse authResponse = authService.register(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "Student registered successfully",
                            "data", authResponse
                    ));

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    @PostMapping("/login")
public ResponseEntity<?> login(
        @Valid @RequestBody LoginRequest request
) {

    try {
        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Login successful",
                        "data", authResponse
                )
        );

    } catch (RuntimeException e) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                ));
    }
}

@PostMapping("/forgot-password")
public ResponseEntity<?> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request
) {

    try {

        authService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message",
                        "If an account exists for this email, a password reset link has been sent."
                )
        );

    } catch (Exception e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        Map.of(
                                "success", false,
                                "message", "Unable to process password reset request"
                        )
                );
    }
}
@PostMapping("/reset-password")
public ResponseEntity<?> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request
) {

    try {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Password reset successfully"
                )
        );

    } catch (RuntimeException e) {

        return ResponseEntity
                .badRequest()
                .body(
                        Map.of(
                                "success", false,
                                "message", e.getMessage()
                        )
                );
    }
}

@GetMapping("/profile")
public ResponseEntity<?> profile(
        Authentication authentication
) {

    if (authentication == null || !authentication.isAuthenticated()) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "success", false,
                        "message", "Authentication required"
                ));
    }

    Student student = (Student) authentication.getPrincipal();

    UserResponse user = new UserResponse(
            student.getId(),
            student.getStudentId(),
            student.getName(),
            student.getEmail(),
            student.getPhone()
    );

    return ResponseEntity.ok(
            Map.of(
                    "success", true,
                    "data", Map.of(
                            "user", user
                    )
            )
    );
}
}