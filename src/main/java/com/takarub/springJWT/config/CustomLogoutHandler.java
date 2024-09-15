package com.takarub.springJWT.config;

import com.takarub.springJWT.model.Token;
import com.takarub.springJWT.reposoitry.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {


    private final TokenRepository tokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
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
