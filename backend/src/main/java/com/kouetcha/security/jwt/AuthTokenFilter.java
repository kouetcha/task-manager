package com.kouetcha.security.jwt;

import com.kouetcha.config.auditor.UserContext;
import com.kouetcha.model.utilisateur.Utilisateur;
import com.kouetcha.repository.utilisateur.UtilisateurRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.Enumeration;

public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Value("${kouetcha.app.jwtSecret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                Authentication authentication = jwtUtils.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                UserContext.setCurrentUser(username);
                //System.out.println("Utilisateur Email::"+username);
                Utilisateur utilisateur=utilisateurRepository.findByEmail(username).get();
                UserContext.setUtilisaeurConnecte(utilisateur);
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();

        // Parcourir tous les noms d'en-tête et afficher leurs valeurs
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            logger.info("Header: " + headerName + " = " + headerValue);
        }
        String bearerToken = request.getHeader("Authorization");
       logger.info(" Authorization fournie: " + bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}