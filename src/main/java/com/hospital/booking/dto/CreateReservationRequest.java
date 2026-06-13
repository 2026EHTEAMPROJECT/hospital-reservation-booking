package com.hospital.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateReservationRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorId;

    // 환자가 자유롭게 선택한 진료 예약 일시(연·월·일·시). 실제 예약 시각으로 저장된다.
    @NotNull
    private LocalDateTime appointmentTime;

    // (구) 고정 슬롯 ID. 자유 일시 선택으로 전환하며 더 이상 필수가 아니다(하위호환 위해 유지).
    private Long scheduleId;

    @NotNull
    private Integer amount;
}
