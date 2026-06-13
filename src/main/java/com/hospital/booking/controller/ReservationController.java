package com.hospital.booking.controller;

import com.hospital.booking.dto.CreateReservationRequest;
import com.hospital.booking.dto.ReservationResponse;
import com.hospital.booking.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                .toList();
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

    @DeleteMapping("/{reservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        reservationService.deleteReservation(reservationId, jwt);
    }

    @GetMapping
    public List<ReservationResponse> getAllReservations() {
        return reservationService.getAllReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }
}