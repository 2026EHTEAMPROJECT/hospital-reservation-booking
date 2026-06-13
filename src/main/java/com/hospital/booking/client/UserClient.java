package com.hospital.booking.client;

import com.hospital.booking.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);

    // 현재 요청자(JWT)의 로컬 사용자 정보. FeignAuthConfig 가 토큰을 릴레이하므로
    // user-service 가 sub→로컬 User.id 매핑된 본인 정보를 돌려준다(예약 삭제 본인 검증용).
    @GetMapping("/api/users/me")
    UserResponse getMe();
}
