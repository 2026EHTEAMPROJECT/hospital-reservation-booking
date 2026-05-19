package com.hospital.booking.dto;

public record PaymentRequestMessage(
        Long reservationId,
        Long patientId,
        Integer amount,
        String patientName
) {}
