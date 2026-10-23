package kr.gilmok.platform.queue.filter;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class QueueLoggingFilterTest {

    @Mock
    private Tracer tracer;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private QueueLoggingFilter queueLoggingFilter;

    @Test
    @DisplayName("Context-Path가 포함된 /queue 요청 유입 시 RDB 블로킹 없이 정상적으로 필터 체인을 통과한다")
    void shouldFilterWhenQueueRequestWithContextPath() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/gilmok-platform");
        request.setServletPath("/queue/enter");
        request.setRequestURI("/gilmok-platform/queue/enter");
        request.setMethod("POST");

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        // When
        boolean skipped = queueLoggingFilter.shouldNotFilter(request);
        queueLoggingFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(skipped).isFalse();
        then(filterChain).should().doFilter(request, response);
    }

    @Test
    @DisplayName("/queue 경로가 아닌 요청은 로깅 필터 대상에서 제외된다")
    void shouldSkipLoggingWhenNonQueueRequest() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/gilmok-platform");
        request.setServletPath("/admin/queues");
        request.setRequestURI("/gilmok-platform/admin/queues");
        request.setMethod("GET");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        boolean skipped = queueLoggingFilter.shouldNotFilter(request);
        queueLoggingFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(skipped).isTrue();
        then(filterChain).should().doFilter(request, response);
    }
}
