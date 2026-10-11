package kr.gilmok.platform.queue.filter;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import kr.gilmok.platform.ai.entity.RequestLog;
import kr.gilmok.platform.ai.repository.RequestLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class QueueLoggingFilterTest {

    @Mock
    private RequestLogRepository requestLogRepository;

    @Mock
    private Tracer tracer;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private QueueLoggingFilter queueLoggingFilter;

    @Test
    @DisplayName("Context-Path가 포함된 /queue 요청 유입 시 정상적으로 RequestLog를 저장한다")
    void shouldLogWhenQueueRequestWithContextPath() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/gilmok-platform");
        request.setServletPath("/queue/enter");
        request.setRequestURI("/gilmok-platform/queue/enter");
        request.setMethod("POST");

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        // When
        queueLoggingFilter.doFilter(request, response, filterChain);

        // Then
        then(filterChain).should().doFilter(request, response);

        ArgumentCaptor<RequestLog> captor = ArgumentCaptor.forClass(RequestLog.class);
        then(requestLogRepository).should().save(captor.capture());

        RequestLog savedLog = captor.getValue();
        assertThat(savedLog.getPath()).isEqualTo("/gilmok-platform/queue/enter");
        assertThat(savedLog.getMethod()).isEqualTo("POST");
        assertThat(savedLog.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("/queue 경로가 아닌 요청은 로깅 필터를 건너뛰고 DB에 저장하지 않는다")
    void shouldSkipLoggingWhenNonQueueRequest() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/gilmok-platform");
        request.setServletPath("/admin/logs");
        request.setRequestURI("/gilmok-platform/admin/logs");
        request.setMethod("GET");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        queueLoggingFilter.doFilter(request, response, filterChain);

        // Then
        then(filterChain).should().doFilter(request, response);
        then(requestLogRepository).should(never()).save(any(RequestLog.class));
    }
}
