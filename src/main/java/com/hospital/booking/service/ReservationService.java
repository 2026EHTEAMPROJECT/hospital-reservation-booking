package com.hospital.booking.service;

import com.hospital.booking.domain.Reservation;
import com.hospital.booking.dto.CreateReservationRequest;
import com.hospital.booking.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public Reservation createReservation(CreateReservationRequest request) {

        Reservation reservation = Reservation.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .scheduleId(request.getScheduleId())
                .status("WAITING")
                .reservationTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        return reservationRepository.save(reservation);
    }
}