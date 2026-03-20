package com.kouetcha.security;

import com.kouetcha.security.jwt.AuthEntryPointJwt;
import com.kouetcha.security.jwt.AuthTokenFilter;
import com.kouetcha.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final RateLimitingFilter rateLimitingFilter;
    private final PasswordEncoder passwordEncoder;
    @Value("${allowed_origin}")
    private String allowedOrigins;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    // AuthenticationManager pour injection dans les controllers
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    private final CorsConfigurationSource corsConfigurationSource;

    // AuthenticationProvider
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {


        String frameAncestors = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .collect(Collectors.joining(" "));

        http
               .cors(cors -> cors.configurationSource(corsConfigurationSource)) 
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/utilisateur/connexion/**"),
                                new AntPathRequestMatcher("/ws/**"),
                                new AntPathRequestMatcher("/utilisateur/image/**"),
                                new AntPathRequestMatcher("/utilisateur/create/**"),
                                new AntPathRequestMatcher("/fichiers-activite/**/download/**"),
                                new AntPathRequestMatcher("/fichiers-tache/**/download/**"),
                                new AntPathRequestMatcher("/fichiers-projet/**/download/**"),
                                new AntPathRequestMatcher("/fichiers-commentaire-activite/**/download/**"),
                                new AntPathRequestMatcher("/fichiers-commentaire-tache/**/download/**"),
                                new AntPathRequestMatcher("/fichiers-commentaire-projet/**/download/**"),
                                new AntPathRequestMatcher("/fichiers-activite/**/onlyoffice-save/**"),
                                new AntPathRequestMatcher("/fichiers-tache/**/onlyoffice-save/**"),
                                new AntPathRequestMatcher("/fichiers-projet/**/onlyoffice-save/**"),
                                new AntPathRequestMatcher("/fichiers-commentaire-activite/**/onlyoffice-save/**"),
                                new AntPathRequestMatcher("/fichiers-commentaire-tache/**/onlyoffice-save/**"),
                                new AntPathRequestMatcher("/fichiers-commentaire-projet/**/onlyoffice-save/**"),
                                new AntPathRequestMatcher("/swagger-ui.html"),
                                new AntPathRequestMatcher("/swagger-ui/**"),
                                new AntPathRequestMatcher("/v2/api-docs"),
                                new AntPathRequestMatcher("/v3/api-docs/**"),
                                new AntPathRequestMatcher("/swagger-resources/**")
                        ).permitAll()
                        .anyRequest().authenticated()
                           )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives(
                                        "frame-ancestors 'self' " + frameAncestors
                                )
                        )
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
