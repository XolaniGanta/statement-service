package com.ganta.microservices.statements.service.impl;

import com.ganta.microservices.statements.exception.StatementErrorCode;
import com.ganta.microservices.statements.exception.StatementException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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

    public void validateForStatement(UUID statementId, String token) {
        Claims claims = parseClaims(token);

        String tokenType = claims.get("type", String.class);
        String tokenStatementId = claims.get("statementId", String.class);

        boolean validToken = "statement-download".equals(tokenType)
                && statementId.toString().equals(tokenStatementId)
                && statementId.toString().equals(claims.getSubject());

        if (!validToken) {
            throw new StatementException(StatementErrorCode.INVALID_DOWNLOAD_LINK);
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new StatementException(StatementErrorCode.LINK_EXPIRED, e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new StatementException(StatementErrorCode.INVALID_DOWNLOAD_LINK, e);
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
