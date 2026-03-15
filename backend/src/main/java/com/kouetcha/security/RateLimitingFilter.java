package com.kouetcha.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    private final Bucket bucket;
    private final Map<String, Bucket> ipBucketMap = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(180, Refill.greedy(180, Duration.ofSeconds(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    public RateLimitingFilter() {
        Bandwidth limit = Bandwidth.classic(160, Refill.greedy(160, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder().addLimit(limit).build();
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String ip = request.getRemoteAddr();
        Bucket bucket = ipBucketMap.computeIfAbsent(ip, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setContentType("Too many requests");
            response.getWriter().write("Too many requests");
        }
    }


}