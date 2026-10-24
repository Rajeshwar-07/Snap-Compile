package com.snapcompile.repository;

import com.snapcompile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * UserRepository - Spring Data JPA handles all DB queries automatically
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (used during login)
    Optional<User> findByEmail(String email);

    // Find user by username
    Optional<User> findByUsername(String username);

    // Check if email already exists (used during registration)
    boolean existsByEmail(String email);

    // Check if username already exists
    boolean existsByUsername(String username);
}
