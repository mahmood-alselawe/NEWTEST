package com.takarub.springJWT.reposoitry;

import com.takarub.springJWT.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByUsername(String username);


    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = ?2 WHERE u.username = ?1")
    void updatePassword(String email, String password);
}