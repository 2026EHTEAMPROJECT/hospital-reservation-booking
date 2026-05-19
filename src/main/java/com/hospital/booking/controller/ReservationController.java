package com.hospital.booking.controller;

import com.hospital.booking.domain.Reservation;
import com.hospital.booking.dto.CreateReservationRequest;
import com.hospital.booking.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public Reservation createReservation(
            @RequestBody CreateReservationRequest request
    ) {

        return reservationService.createReservation(request);
    }
}