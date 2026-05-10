package com.ganta.microservices.statements.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class StatementDownloadJwtService {

    @Value("${statement.download.jwt-secret}")
    private String jwtSecret;

    @Value("${statement.download.expiry-minutes:5}")
    private long expiryMinutes;

    public String generateToken(UUID statementId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expiryMinutes * 60);

        return Jwts.builder()
                .subject(statementId.toString())
                .claim("type", "statement-download")
                .claim("statementId", statementId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSigningKey())
                .compact();
    }

    public Instant getTokenExpiry(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().toInstant();
    }

    public boolean isValidForStatement(UUID statementId, String token) {
        try {
            Claims claims = parseClaims(token);

            String tokenType = claims.get("type", String.class);
            String tokenStatementId = claims.get("statementId", String.class);

            return "statement-download".equals(tokenType)
                    && statementId.toString().equals(tokenStatementId)
                    && statementId.toString().equals(claims.getSubject());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
