package com.hospital.booking.dto;

public record BookingNotificationMessage(
        Long reservationId,
        Long patientId,
        Long doctorId,
        String status,
        String reservationTime,
        String patientName,
        String doctorName
) {}
