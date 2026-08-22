package com.lostandfound.service;

import com.lostandfound.dto.AuthResponse;
import com.lostandfound.dto.LoginRequest;
import com.lostandfound.dto.RegisterRequest;
import com.lostandfound.dto.UserResponse;
import com.lostandfound.entity.Student;
import com.lostandfound.repository.StudentRepository;
import com.lostandfound.security.JwtService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import com.lostandfound.dto.ResetPasswordRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    @Value("${app.frontend.url}")
private String frontendUrl;

    public AuthService(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService
    ) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }
     
    public AuthResponse register(RegisterRequest request) {

    if (studentRepository.existsByStudentId(request.getStudentId())) {
        throw new RuntimeException("Student ID already exists");
    }

    if (studentRepository.existsByEmail(request.getEmail())) {
        throw new RuntimeException("Email already exists");
    }

    Student student = new Student();

    student.setStudentId(request.getStudentId());
    student.setName(request.getName());
    student.setEmail(request.getEmail());
    student.setPhone(request.getPhone());

    String hashedPassword =
            passwordEncoder.encode(request.getPassword());

    student.setPasswordHash(hashedPassword);

    Student savedStudent = studentRepository.save(student);

    String token = jwtService.generateToken(
            savedStudent.getId(),
            savedStudent.getStudentId(),
            savedStudent.getEmail()
    );

    UserResponse user = new UserResponse(
            savedStudent.getId(),
            savedStudent.getStudentId(),
            savedStudent.getName(),
            savedStudent.getEmail(),
            savedStudent.getPhone()
    );

    return new AuthResponse(user, token);
}

public AuthResponse login(LoginRequest request) {

    Student student = studentRepository
            .findByStudentId(request.getStudentId())
            .orElse(null);

    if (student == null) {
        throw new RuntimeException("Invalid student ID or password");
    }

    boolean passwordMatches = passwordEncoder.matches(
            request.getPassword(),
            student.getPasswordHash()
    );

    if (!passwordMatches) {
        throw new RuntimeException("Invalid student ID or password");
    }

    String token = jwtService.generateToken(
            student.getId(),
            student.getStudentId(),
            student.getEmail()
    );

    UserResponse user = new UserResponse(
            student.getId(),
            student.getStudentId(),
            student.getName(),
            student.getEmail(),
            student.getPhone()
    );

    return new AuthResponse(user, token);
}

public void forgotPassword(String email) {

    // Always return the same outward behavior, whether the account exists or not.
    Student student = studentRepository.findByEmail(email).orElse(null);

    if (student == null) {
        return;
    }

    SecureRandom secureRandom = new SecureRandom();
    byte[] tokenBytes = new byte[32];
    secureRandom.nextBytes(tokenBytes);

    String resetToken = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(tokenBytes);

    student.setPasswordResetToken(resetToken);
    student.setPasswordResetExpiry(
            LocalDateTime.now().plusMinutes(15)
    );

    studentRepository.save(student);

    String resetLink =
        frontendUrl + "/reset-password.html?token=" + resetToken;

    emailService.sendPasswordResetEmail(
            student.getEmail(),
            student.getName(),
            resetLink
    );
}

public void resetPassword(ResetPasswordRequest request) {

    Student student = studentRepository
            .findByPasswordResetToken(request.getToken())
            .orElseThrow(() ->
                    new RuntimeException("Invalid or expired reset token")
            );

    if (student.getPasswordResetExpiry() == null
            || student.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {

        throw new RuntimeException("Invalid or expired reset token");
    }

    String hashedPassword =
            passwordEncoder.encode(request.getNewPassword());

    student.setPasswordHash(hashedPassword);

    // Invalidate the reset token immediately after successful reset
    student.setPasswordResetToken(null);
    student.setPasswordResetExpiry(null);

    studentRepository.save(student);
}
  
}