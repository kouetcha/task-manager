package com.kouetcha.security.jwt;

import com.kouetcha.model.utilisateur.Utilisateur;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${kouetcha.app.jwtSecret}")
    private String jwtSecret;

    @Value("${kouetcha.app.jwtExpirationMs}")
    private long jwtDuration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Utilisateur utilisateur) {
        Key key = getSigningKey();
        return Jwts.builder()
                .setSubject(utilisateur.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtDuration))
                .signWith(key, SignatureAlgorithm.HS512)
                .claim("permissions", List.of())
                .claim("utilisateurId", utilisateur.getId())
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        List<LinkedHashMap<String, String>> permissionsMap = (List<LinkedHashMap<String, String>>) claims.get("permissions");

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (permissionsMap != null) {
            for (LinkedHashMap<String, String> permissionMap : permissionsMap) {
                String authority = permissionMap.get("authority");
                authorities.add(new SimpleGrantedAuthority(authority));
            }
        }

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            logger.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Long extractPersonnelId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("personnelId", Long.class);
    }

    public String generateSimpleToken(Utilisateur utilisateur, int minutes) {
        Key key = getSigningKey();
        return Jwts.builder()
                .setSubject(utilisateur.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + minutes * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS512)
                .claim("permissions", List.of())
                .claim("utilisateurId", utilisateur.getId())
                .compact();
    }
}
