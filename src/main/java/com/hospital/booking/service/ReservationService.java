package com.hospital.booking.service;

import com.hospital.booking.client.DoctorClient;
import com.hospital.booking.client.UserClient;
import com.hospital.booking.domain.Reservation;
import com.hospital.booking.dto.CreateReservationRequest;
import com.hospital.booking.event.ReservationCreatedEvent;
import com.hospital.booking.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserClient userClient;
    private final DoctorClient doctorClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Reservation createReservation(CreateReservationRequest request) {
        validate(request);

        String patientName = resolvePatientName(request.getPatientId());
        String doctorName = resolveDoctorName(request.getDoctorId());

        Reservation reservation = Reservation.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .scheduleId(request.getScheduleId())
                .amount(request.getAmount())
                .status("WAITING")
                .reservationTime(LocalDateTime.now())
                .build();

        Reservation savedReservation =
                reservationRepository.save(reservation);

        eventPublisher.publishEvent(
                new ReservationCreatedEvent(
                        savedReservation.getId(),
                        savedReservation.getPatientId(),
                        savedReservation.getDoctorId(),
                        savedReservation.getStatus(),
                        savedReservation.getReservationTime(),
                        patientName,
                        doctorName,
                        savedReservation.getAmount()
                )
        );

        Map<String, Object> message = new HashMap<>();
        message.put("reservationId", savedReservation.getId());
        message.put("patientId", savedReservation.getPatientId());
        message.put("doctorId", savedReservation.getDoctorId());
        message.put("status", savedReservation.getStatus());
        message.put(
                "reservationTime",
                savedReservation.getReservationTime().toString()
        );

        return savedReservation;
    }

    private String resolvePatientName(Long patientId) {
        try {
            String name = userClient.getUserById(patientId).name();
            return (name != null && !name.isBlank()) ? name : "환자";
        } catch (Exception e) {
            log.warn(
                    "[사용자 정보 조회 실패] patientId={}, message={}",
                    patientId,
                    e.getMessage()
            );
            return "환자";
        }
    }

    private String resolveDoctorName(Long doctorId) {
        try {
            String name = doctorClient.getDoctorById(doctorId).name();
            return (name != null && !name.isBlank()) ? name : "담당의";
        } catch (Exception e) {
            log.warn(
                    "[의사 정보 조회 실패] doctorId={}, message={}",
                    doctorId,
                    e.getMessage()
            );
            return "담당의";
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

    public List<Reservation> getReservationsByPatient(Long patientId) {

        return reservationRepository.findByPatientId(patientId);
    }

    public Reservation confirmReservation(Long reservationId) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "예약이 존재하지 않습니다."
                                )
                        );

        reservation.updateStatus("CONFIRMED");

        Reservation savedReservation =
                reservationRepository.save(reservation);

        Map<String, Object> message = new HashMap<>();
        message.put("reservationId", savedReservation.getId());
        message.put("patientId", savedReservation.getPatientId());
        message.put("doctorId", savedReservation.getDoctorId());
        message.put("status", savedReservation.getStatus());

        rabbitTemplate.convertAndSend(
                "hospital.exchange",
                "booking.notification",
                message
        );

        return savedReservation;
    }

    public List<Reservation> getAllReservations() {

        return reservationRepository.findAll();
    }

    public Reservation cancelReservation(Long reservationId) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "예약이 존재하지 않습니다."
                                )
                        );

        reservation.updateStatus("CANCELED");

        Reservation savedReservation =
                reservationRepository.save(reservation);

        Map<String, Object> message = new HashMap<>();
        message.put("reservationId", savedReservation.getId());
        message.put("patientId", savedReservation.getPatientId());
        message.put("doctorId", savedReservation.getDoctorId());
        message.put("status", savedReservation.getStatus());

        rabbitTemplate.convertAndSend(
                "hospital.exchange",
                "booking.notification",
                message
        );

        return savedReservation;
    }
}