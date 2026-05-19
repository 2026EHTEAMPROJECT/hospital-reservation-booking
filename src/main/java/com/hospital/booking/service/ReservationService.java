package com.hospital.booking.service;

import com.hospital.booking.client.UserClient;
import com.hospital.booking.domain.Reservation;
import com.hospital.booking.dto.CreateReservationRequest;
import com.hospital.booking.event.ReservationCreatedEvent;
import com.hospital.booking.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserClient userClient;

    @Transactional
    public Reservation createReservation(CreateReservationRequest request) {
        validate(request);

        String patientName = resolvePatientName(request.getPatientId());

        Reservation reservation = Reservation.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .scheduleId(request.getScheduleId())
                .amount(request.getAmount())
                .status("WAITING")
                .reservationTime(LocalDateTime.now())
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("[예약 생성 완료] reservationId={}, status={}", saved.getId(), saved.getStatus());

        eventPublisher.publishEvent(new ReservationCreatedEvent(
                saved.getId(),
                saved.getPatientId(),
                saved.getDoctorId(),
                saved.getStatus(),
                saved.getReservationTime(),
                patientName,
                "Doctor-" + saved.getDoctorId(),
                saved.getAmount()
        ));

        return saved;
    }

    private String resolvePatientName(Long patientId) {
        try {
            return userClient.getUserById(patientId).name();
        } catch (Exception e) {
            log.warn("[사용자 정보 조회 실패] patientId={}, message={}", patientId, e.getMessage());
            return "환자";
        }
    }

    private void validate(CreateReservationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청은 null일 수 없습니다.");
        }
        if (request.getPatientId() == null) {
            throw new IllegalArgumentException("patientId는 null일 수 없습니다.");
        }
        if (request.getDoctorId() == null) {
            throw new IllegalArgumentException("doctorId는 null일 수 없습니다.");
        }
        if (request.getScheduleId() == null) {
            throw new IllegalArgumentException("scheduleId는 null일 수 없습니다.");
        }
        if (request.getAmount() == null || request.getAmount() < 0) {
            throw new IllegalArgumentException("amount는 0 이상이어야 합니다.");
        }
    }
}
