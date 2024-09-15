package com.takarub.springJWT.reposoitry;

import com.takarub.springJWT.model.RefreshToken;
import com.takarub.springJWT.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken , Integer> {
     Optional<RefreshToken> findByRefreshToken(String refreshToken);
     Optional<RefreshToken> findByUser(User user); // Add this method
}
