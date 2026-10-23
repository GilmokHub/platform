package kr.gilmok.platform.queue.filter;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class QueueLoggingFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    public QueueLoggingFilter(@Autowired(required = false) Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath != null && !servletPath.isBlank()) {
            return !servletPath.startsWith("/queue");
        }
        String path = request.getRequestURI();
        return !path.contains("/queue");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        String requestId = (tracer != null && tracer.currentSpan() != null)
                ? tracer.currentSpan().context().traceId()
                : request.getHeader("X-Request-Id");

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            log.info("type=QUEUE_RESPONSE requestId={} method={} path={} status={} latency={}ms",
                    requestId, method, path, status, duration);
        }
    }
}
