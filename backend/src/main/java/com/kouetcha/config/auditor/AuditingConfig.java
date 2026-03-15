package com.kouetcha.config.auditor;

import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

//@Configuration
//@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl();
    }
}