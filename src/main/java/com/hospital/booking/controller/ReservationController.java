package com.hospital.booking.controller;

import com.hospital.booking.domain.Reservation;
import com.hospital.booking.dto.CreateReservationRequest;
import com.hospital.booking.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/patient/{patientId}")
    public List<Reservation> getReservationsByPatient(
            @PathVariable Long patientId
    ) {

        return reservationService.getReservationsByPatient(patientId);
    }

    @PutMapping("/{reservationId}/confirm")
        public Reservation confirmReservation(
            @PathVariable Long reservationId
        ) {

            return reservationService.confirmReservation(reservationId);
    }

    @PutMapping("/{reservationId}/cancel")
        public Reservation cancelReservation(
            @PathVariable Long reservationId
        ) {

            return reservationService.cancelReservation(reservationId);
        }

    @GetMapping
        public List<Reservation> getAllReservations() {

        return reservationService.getAllReservations();
    }
}