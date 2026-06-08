package com.hospital.booking.dto;

// 예약 취소 시 payment-service 에 환불을 요청하는 메시지.
// 환불 금액은 payment 가 reservationId 로 결제내역을 조회해 결정한다.
public record RefundRequestMessage(
        Long reservationId,
        Long patientId
) {
}
