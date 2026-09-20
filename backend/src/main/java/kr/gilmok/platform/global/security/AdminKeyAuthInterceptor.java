package kr.gilmok.platform.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "app.admin")
public class AdminKeyAuthInterceptor implements HandlerInterceptor {

    public static final String HEADER_ADMIN_KEY = "X-Platform-Admin-Key";
    public static final String HEADER_ADMIN_USER_ID = "X-Admin-User-Id";
    public static final String ATTR_TENANT_CODE = "tenantCode";
    private static final String ERROR_RESPONSE_BODY = "{\"status\":\"error\",\"code\":\"A001\",\"message\":\"유효하지 않은 관리자 PassKey입니다.\"}";

    /**
     * 고객사 및 마스터 관리자 PassKey 매핑 (application.yml: app.admin.keys)
     * 예: master -> gmk_master_..., demo -> gmk_demo_...
     */
    private Map<String, String> keys = new HashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        String clientKey = request.getHeader(HEADER_ADMIN_KEY);
        if (clientKey == null || clientKey.isBlank()) {
            return reject(request, response);
        }

        // 고객사/마스터별 PassKey 대조 및 테넌트 식별 (빈 값 매칭 방지)
        String matchedTenant = null;
        if (keys != null && !keys.isEmpty()) {
            for (Map.Entry<String, String> entry : keys.entrySet()) {
                String registeredKey = entry.getValue();
                if (registeredKey != null && !registeredKey.isBlank() && clientKey.equals(registeredKey)) {
                    matchedTenant = entry.getKey();
                    break;
                }
            }
        }

        if (matchedTenant == null) {
            return reject(request, response);
        }

        // 인가 성공: 식별된 테넌트 코드 주입 (master, demo 등)
        request.setAttribute(ATTR_TENANT_CODE, matchedTenant);
        String adminUserId = request.getHeader(HEADER_ADMIN_USER_ID);
        log.info("[AdminAuth] 관리자 인가 성공 - Tenant: {}, AdminId: {}, URI: {}",
                matchedTenant, (adminUserId != null && !adminUserId.isBlank()) ? adminUserId : "admin", request.getRequestURI());

        return true;
    }

    private boolean reject(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.warn("[AdminAuth] 관리자 PassKey 인증 실패 - URI: {}, IP: {}", request.getRequestURI(), request.getRemoteAddr());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(ERROR_RESPONSE_BODY);
        return false;
    }
}
