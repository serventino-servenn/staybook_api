package com.staybook.api.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.staybook.api.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
