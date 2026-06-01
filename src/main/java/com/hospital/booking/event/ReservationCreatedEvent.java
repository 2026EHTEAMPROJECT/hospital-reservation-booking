package com.hospital.booking.event;

import java.time.LocalDateTime;

public record ReservationCreatedEvent(
        Long reservationId,
        Long patientId,
        Long doctorId,
        String status,
        LocalDateTime reservationTime,
        String patientName,
        String doctorName,
        Integer amount
) {}
