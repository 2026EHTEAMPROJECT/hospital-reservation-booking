package com.hospital.booking.client;

import com.hospital.booking.dto.DoctorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doctor-client", url = "${user-service.url}")
public interface DoctorClient {

    @GetMapping("/api/doctors/{id}")
    DoctorResponse getDoctorById(@PathVariable("id") Long id);
}
