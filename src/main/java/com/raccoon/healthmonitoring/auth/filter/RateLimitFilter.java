package com.raccoon.healthmonitoring.auth.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    private final Map<String, RateLimitData> requestData = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);

        // Cleanup for old entries
        cleanupOldEntries();

        // Get or create rate limit data for this IP
        RateLimitData data = requestData.computeIfAbsent(clientIp, k -> new RateLimitData());

        synchronized (data) {
            Instant now = Instant.now();

            if (Duration.between(data.windowStart, now).compareTo(WINDOW_DURATION) >= 0) {
                data.windowStart = now;
                data.requestCount = 0;
            }

            data.requestCount++;

            if (data.requestCount > MAX_REQUESTS_PER_MINUTE) {
                long secondsUntilReset = WINDOW_DURATION.getSeconds() -
                                        Duration.between(data.windowStart, now).getSeconds();

                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
                httpResponse.setContentType("application/json");
                httpResponse.setHeader("Retry-After", String.valueOf(secondsUntilReset));
                httpResponse.getWriter().write(String.format(
                    "{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded. Try again in %d seconds.\",\"retryAfter\":%d}",
                    secondsUntilReset, secondsUntilReset
                ));
                return;
            }
        }

        chain.doFilter(request, response);
    }

    // Get client IP address from request headers
    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void cleanupOldEntries() {
        if (requestData.size() > 10000) {
            Instant cutoff = Instant.now().minus(WINDOW_DURATION).minus(Duration.ofMinutes(5));
            requestData.entrySet().removeIf(entry ->
                entry.getValue().windowStart.isBefore(cutoff)
            );
        }
    }

    private static class RateLimitData {
        Instant windowStart = Instant.now();
        int requestCount = 0;
    }
}
