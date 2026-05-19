package com.hospital.booking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReservationRequest {

    private Long patientId;

    private Long doctorId;

    private Long scheduleId;

    private Integer amount;
}
