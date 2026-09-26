package com.site.webapp.config;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpStatus;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

/**
 * Фильтр для rate limiting загрузки файлов.
 * Ограничивает количество запросов на загрузку файлов с одного IP.
 * Использует Bucket4j для реализации token bucket алгоритма.
 * LinkedHashMap с LRU для автоматической очистки старых записей.
 */

@Component
public class FileRateLimitFilter extends OncePerRequestFilter{
    private static final int MAX_IP_ENTRIES = 5_000;

    private final Map<String, Bucket> buckets = new LinkedHashMap<>(MAX_IP_ENTRIES, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Bucket> eldest) {
            return size() > MAX_IP_ENTRIES;
        }
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean isFileEndpoint = path.contains("/avatar")
                || path.contains("/attachment")
                || path.contains("/files");

        boolean isWriteOperation = "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
        return !(isFileEndpoint && isWriteOperation);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);

        Bucket bucket;
        synchronized (buckets) {
            bucket = buckets.computeIfAbsent(clientIp, k -> createNewBucket());
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        }
        else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("""
                {
                    "error": "TOO_MANY_REQUESTS",
                    "message": "Превышен лимит операций с файлами. Попробуйте позже."
                }
                """);
        }
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

}
