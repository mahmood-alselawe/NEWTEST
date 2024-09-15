package com.takarub.springJWT.service;

import com.takarub.springJWT.model.RefreshToken;
import com.takarub.springJWT.model.User;
import com.takarub.springJWT.reposoitry.RefreshTokenRepository;
import com.takarub.springJWT.reposoitry.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {



    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    public RefreshToken createOrUpdateRefreshToken(User user) {
        Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByUser(user);
        RefreshToken refreshToken;

        if (existingTokenOpt.isPresent()) {
            // Update existing token

            refreshToken = existingTokenOpt.get();
            String newRefreshToken = jwtService.generateRefreshToken(user);
            refreshToken.setRefreshToken(newRefreshToken);
            refreshToken.setExpirationToken(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000)); // 7 days
        } else {
            // Create new token
            String newRefreshToken = jwtService.generateRefreshToken(user);
            refreshToken = RefreshToken.builder()
                    .user(user)
                    .refreshToken(newRefreshToken)
                    .expirationToken(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000)) // 7 days
                    .build();
        }

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByRefreshToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpirationToken().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token is expired. Please make a new login.");
        }
        return token;
    }
}
