package com.hospital.booking.dto;

import com.hospital.booking.domain.Reservation;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long patientId,
        Long doctorId,
        Long scheduleId,
        Integer amount,
        String status,
        LocalDateTime reservationTime,
        LocalDateTime createdAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getPatientId(),
                reservation.getDoctorId(),
                reservation.getScheduleId(),
                reservation.getAmount(),
                reservation.getStatus(),
                reservation.getReservationTime(),
                reservation.getCreatedAt()
        );
    }
}
