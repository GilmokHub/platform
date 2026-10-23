package kr.gilmok.platform.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.gilmok.platform.global.dto.ErrorResponse;
import kr.gilmok.platform.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        log.warn("Unauthorized Access Attempt - URI: [{}], Message: [{}]", request.getRequestURI(), authException.getMessage());

        meterRegistry.counter("token.validation", "result", "failure").increment();

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = ErrorResponse.of(GlobalErrorCode.UNAUTHORIZED);

        if (authException instanceof BadCredentialsException || authException instanceof UsernameNotFoundException) {
            errorResponse = ErrorResponse.of(GlobalErrorCode.INVALID_USER);
        }

        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}
