package com.hospital.booking.controller;

import com.hospital.booking.dto.CreateReservationRequest;
import com.hospital.booking.dto.ReservationResponse;
import com.hospital.booking.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ReservationResponse createReservation(
            @Valid @RequestBody CreateReservationRequest request
    ) {
        return ReservationResponse.from(reservationService.createReservation(request));
    }

    @GetMapping("/patient/{patientId}")
    public List<ReservationResponse> getReservationsByPatient(
            @PathVariable Long patientId
    ) {
        return reservationService.getReservationsByPatient(patientId).stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    @PutMapping("/{reservationId}/confirm")
    public ReservationResponse confirmReservation(
            @PathVariable Long reservationId
    ) {
        return ReservationResponse.from(reservationService.confirmReservation(reservationId));
    }

    @PutMapping("/{reservationId}/cancel")
    public ReservationResponse cancelReservation(
            @PathVariable Long reservationId
    ) {
        return ReservationResponse.from(reservationService.cancelReservation(reservationId));
    }

    @GetMapping
    public List<ReservationResponse> getAllReservations() {
        return reservationService.getAllReservations().stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }
}