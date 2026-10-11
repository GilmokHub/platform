package kr.gilmok.platform.queue.filter;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.gilmok.platform.ai.entity.RequestLog;
import kr.gilmok.platform.ai.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class QueueLoggingFilter extends OncePerRequestFilter {

    private final RequestLogRepository requestLogRepository;
    private final Tracer tracer;

    public QueueLoggingFilter(RequestLogRepository requestLogRepository,
                              @Autowired(required = false) Tracer tracer) {
        this.requestLogRepository = requestLogRepository;
        this.tracer = tracer;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // /queue 로 시작하지 않는 비즈니스 요청은 로깅을 건너뜁니다.
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
        LocalDateTime requestTime = LocalDateTime.now();

        // 1. Trace ID / Request ID 추출 (Micrometer Tracer 우선 -> 헤더 -> UUID 순)
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

            try {
                RequestLog logEntity = RequestLog.builder()
                        .requestId(requestId)
                        .path(path)
                        .method(method)
                        .status(status)
                        .latencyMs(duration)
                        .timestamp(requestTime)
                        .build();

                requestLogRepository.save(logEntity);
                log.info("type=QUEUE_RESPONSE requestId={} method={} path={} status={} latency={}ms",
                        requestId, method, path, status, duration);
            } catch (Exception e) {
                // 로그 저장 실패가 클라이언트 응답 실패로 전파되지 않도록 장애 격리
                log.error("type=QUEUE_LOG_SAVE_ERROR requestId={} msg={}", requestId, e.getMessage());
            }
        }
    }
}
