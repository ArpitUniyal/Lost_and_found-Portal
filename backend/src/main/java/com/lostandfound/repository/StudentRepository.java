package com.lostandfound.repository;

import com.lostandfound.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    Optional<Student> findByStudentId(Integer studentId);

    Optional<Student> findByEmail(String email);

    boolean existsByStudentId(Integer studentId);

    boolean existsByEmail(String email);

    Optional<Student> findByPasswordResetToken(String passwordResetToken);
}
