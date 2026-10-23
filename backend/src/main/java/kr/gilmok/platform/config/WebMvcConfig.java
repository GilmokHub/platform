package kr.gilmok.platform.config;

import kr.gilmok.platform.global.security.AdminKeyAuthInterceptor;
import kr.gilmok.platform.queue.interceptor.QueueRateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminKeyAuthInterceptor adminKeyAuthInterceptor;

    @Autowired(required = false)
    private QueueRateLimitInterceptor queueRateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (queueRateLimitInterceptor != null) {
            registry.addInterceptor(queueRateLimitInterceptor)
                    .addPathPatterns("/queue/**");
        }

        registry.addInterceptor(adminKeyAuthInterceptor)
                .addPathPatterns("/admin/**");
    }
}
