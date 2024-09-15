package com.takarub.springJWT.service;

import com.takarub.springJWT.dto.AuthenticationResponse;
import com.takarub.springJWT.model.RefreshToken;
import com.takarub.springJWT.model.Token;
import com.takarub.springJWT.model.User;
//import com.takarub.springJWT.reposoitry.TokenRepository;
import com.takarub.springJWT.reposoitry.RefreshTokenRepository;
import com.takarub.springJWT.reposoitry.TokenRepository;
import com.takarub.springJWT.reposoitry.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;


    public AuthenticationResponse register(User request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        repository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveToken(accessToken, user);

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    private void saveRefreshToken(String refreshToken, User user) {
        RefreshToken token = RefreshToken.builder()
                .refreshToken(refreshToken)
                .expirationToken(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000)) // 7 days
                .user(user)
                .build();

        refreshTokenRepository.save(token);
    }



    public AuthenticationResponse authenticate(User request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Revoke all old tokens for the user
        revokeAllTokensByUser(user);

        // Save the new access token
        saveToken(accessToken, user);

        // Save or update the refresh token
        Optional<RefreshToken> existingRefreshTokenOpt = refreshTokenRepository.findByUser(user);
        if (existingRefreshTokenOpt.isPresent()) {
            RefreshToken existingRefreshToken = existingRefreshTokenOpt.get();
            existingRefreshToken.setRefreshToken(refreshToken);
            existingRefreshToken.setExpirationToken(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000)); // 7 days
            refreshTokenRepository.save(existingRefreshToken);
        } else {
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .refreshToken(refreshToken)
                    .expirationToken(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000)) // 7 days
                    .user(user)
                    .build();
            refreshTokenRepository.save(newRefreshToken);
        }

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request) {
        String token = jwtService.extractTokenFromHeader(request);
        String username = jwtService.extractUsername(token);

        Optional<RefreshToken> refreshTokenOpt = refreshTokenService.findByToken(token);
        if (refreshTokenOpt.isEmpty() || !refreshTokenService.verifyExpiration(refreshTokenOpt.get()).equals(refreshTokenOpt.get())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

        return new ResponseEntity<>(new AuthenticationResponse(newAccessToken, newRefreshToken.getRefreshToken()), HttpStatus.OK);
    }

    private void saveToken(String accessToken, User user) {
        Token token = Token.builder()
                .token(accessToken)
                .isLoggedOut(false)
                .user(user)
                .build();

        tokenRepository.save(token);
    }

    private void revokeAllTokensByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllAccessTokensByUser(user.getId());

        if (!validTokens.isEmpty()) {
            validTokens.forEach(t -> t.setLoggedOut(true));
            tokenRepository.saveAll(validTokens);
        }
    }
    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Token existingToken = tokenRepository.findByToken(token).orElse(null);

            if (existingToken != null) {
                existingToken.setLoggedOut(true);
                tokenRepository.save(existingToken);
            }
        }
    }
}

