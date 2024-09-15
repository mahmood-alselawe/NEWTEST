package com.takarub.springJWT.service;

import com.takarub.springJWT.model.Role;
import com.takarub.springJWT.model.User;
import com.takarub.springJWT.reposoitry.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoder;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final String SECRET_HEY =
            "61cb129e2175c2a9d0b387bca1e2fd3d2bc4cbcc781d12ed8f6d9c7734da84d5";

    private final TokenRepository tokenRepository;
    public boolean isValid(String token, UserDetails user) {
        String username = extractUsername(token);

        boolean isValidToken = tokenRepository.findByToken(token).map(t->!t.isLoggedOut())
                .orElse(false);
        return (username.equals(user.getUsername())&& !isTokenExpired(token)) && isValidToken;
    }

    public boolean isValidRefreshToken(String token, User user) {
        String username = extractUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClimes(token, Claims::getExpiration);
    }

    public String extractUsername(String token){
        return extractClimes(token , Claims::getSubject);

    }
    private <T> T extractClimes(String token ,Function<Claims,T> resolver){
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);

    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public String generateAccessToken(User user) {
        return generateToken(user, 24*60*60*1000);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user,7*24*60*60*1000); // 7 days
    }

    public String generateToken(User user , long expiration ){

        Role role = user.getRole();

        return Jwts
                .builder()
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration ))
                .signWith(getSigninKey())
                .compact();
    }

    private SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(SECRET_HEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String extractTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        return header.substring(7); // Remove "Bearer " prefix
    }


}
