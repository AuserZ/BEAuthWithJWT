package com.example.authbackend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.logging.Logger;

@Service
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;
    
    @Getter
    private Key key;
    
    @PostConstruct
    public void init() {
        key = Jwts.SIG.HS256.key().build();
    }
    
    Logger logger = Logger.getLogger(JwtUtil.class.getName());

    public String generateAccessToken(String id){
        return generateToken(id, ACCESS_TOKEN_EXPIRATION, "access");
    }

    public String generateRefreshToken(String id){
        return generateToken(id, REFRESH_TOKEN_EXPIRATION, "refresh");
    }

    public String generateToken(String id, long expiration, String tokenType){
        return Jwts.builder()
                .setSubject(id)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .issuedAt(new Date())
                .claim("type", tokenType)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    public boolean validateToken(String token){
        try {
//            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.info("Token expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.info("Unsupported JWT: " + e.getMessage());
        } catch (MalformedJwtException e) {
            logger.info("Malformed JWT: " + e.getMessage());
        } catch (SignatureException e) {
            logger.info("Invalid signature: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.info("Illegal argument token: " + e.getMessage());
        }
        return false;
    }

    // Extract claims from token
    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expirationDate.before(new Date());
    }
}
