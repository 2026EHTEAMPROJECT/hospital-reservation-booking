package com.hospital.booking.service;

import com.hospital.booking.client.DoctorClient;
import com.hospital.booking.client.UserClient;
import com.hospital.booking.domain.Reservation;
import com.hospital.booking.dto.BookingNotificationMessage;
import com.hospital.booking.dto.CreateReservationRequest;
import com.hospital.booking.dto.RefundRequestMessage;
import com.hospital.booking.event.ReservationCreatedEvent;
import com.hospital.booking.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.booking.config.RabbitConfig;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    public static final String STATUS_WAITING   = "WAITING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELED  = "CANCELED";
    public static final String STATUS_REFUNDED  = "REFUNDED";

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
                .patientName(patientName)
                .doctorName(doctorName)
                .scheduleId(request.getScheduleId())
                .amount(request.getAmount())
                .status(STATUS_WAITING)
                // 환자가 고른 진료 예약 일시를 그대로 저장한다(과거엔 now() 로 박혀 선택 시각이 버려졌다).
                .reservationTime(request.getAppointmentTime())
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
        if (request.getAppointmentTime() == null) {
            throw new IllegalArgumentException("appointmentTime(예약 일시)은 null일 수 없습니다.");
        }
        if (request.getAmount() == null || request.getAmount() < 0) {
            throw new IllegalArgumentException("amount는 0 이상이어야 합니다.");
        }
    }

    public List<Reservation> getReservationsByPatient(Long patientId) {

        return reservationRepository.findByPatientId(patientId);
    }

    @Transactional
    public Reservation confirmReservation(Long reservationId) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "예약이 존재하지 않습니다."
                                )
                        );

        if (!STATUS_WAITING.equals(reservation.getStatus())) {
            throw new IllegalStateException(
                    "대기 중인 예약만 확정할 수 있습니다. 현재 상태: " + reservation.getStatus()
            );
        }

        reservation.updateStatus(STATUS_CONFIRMED);

        Reservation savedReservation =
                reservationRepository.save(reservation);

        publishStatusChangeNotification(savedReservation);

        return savedReservation;
    }

    public List<Reservation> getAllReservations() {

        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "예약이 존재하지 않습니다."
                                )
                        );

        if (STATUS_CANCELED.equals(reservation.getStatus())) {
            throw new IllegalStateException(
                    "이미 취소된 예약입니다."
            );
        }

        reservation.updateStatus(STATUS_CANCELED);

        Reservation savedReservation =
                reservationRepository.save(reservation);

        publishStatusChangeNotification(savedReservation);
        publishRefundRequest(savedReservation);

        return savedReservation;
    }

    // 취소·환불 완료된 예약만 화면에서 영구 삭제할 수 있다(진행 중 예약은 삭제 불가).
    @Transactional
    public void deleteReservation(Long reservationId) {
        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "예약이 존재하지 않습니다."
                                )
                        );

        String status = reservation.getStatus();
        if (!STATUS_CANCELED.equals(status) && !STATUS_REFUNDED.equals(status)) {
            throw new IllegalStateException(
                    "취소 또는 환불된 예약만 삭제할 수 있습니다. 현재 상태: " + status
            );
        }

        reservationRepository.delete(reservation);
    }

    // 예약 취소 시 payment-service 에 환불을 요청한다(결제내역이 없으면 payment 쪽에서 무시).
    private void publishRefundRequest(Reservation reservation) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.PAYMENT_REFUND_KEY,
                new RefundRequestMessage(
                        reservation.getId(),
                        reservation.getPatientId()
                )
        );
    }

    private void publishStatusChangeNotification(Reservation reservation) {
        // 확정/취소 알림에도 환자·담당의 이름과 예약 일시를 담아 보낸다(과거엔 ID만 보내 이름이 비어 있었다).
        String reservationTime = reservation.getReservationTime() != null
                ? reservation.getReservationTime().toString()
                : null;

        BookingNotificationMessage message = new BookingNotificationMessage(
                reservation.getId(),
                reservation.getPatientId(),
                reservation.getDoctorId(),
                reservation.getStatus(),
                reservationTime,
                reservation.getPatientName(),
                reservation.getDoctorName()
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.BOOKING_NOTIFICATION_KEY,
                message
        );
    }
}