package com.hospital.booking.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 서비스 간(booking -> user/doctor) Feign 호출 시, 현재 사용자의 Authorization
 * 헤더(Keycloak JWT)를 다운스트림으로 전파한다.
 *
 * 이게 없으면 user-service의 oauth2-resource-server 및 Istio AuthorizationPolicy가
 * 토큰 없는 내부 호출을 401/403으로 거부한다(예약 시 환자/의사 이름 조회 실패).
 *
 * 참고: RabbitMQ 컨슈머처럼 HTTP 요청 컨텍스트가 없는 경로에서는 릴레이할 토큰이
 * 없으므로, 그런 서비스(notification 등)는 service-account(client credentials)
 * 토큰을 별도로 발급받아야 한다(README P1).
 */
@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor authForwardInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader("Authorization");
            if (authorization != null && !authorization.isBlank()) {
                template.header("Authorization", authorization);
            }
        };
    }
}
