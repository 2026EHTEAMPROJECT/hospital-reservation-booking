package com.hospital.booking.service;

import com.hospital.booking.domain.Reservation;
import com.hospital.booking.dto.CreateReservationRequest;
import com.hospital.booking.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RabbitTemplate rabbitTemplate;

    public Reservation createReservation(CreateReservationRequest request) {

        Reservation reservation = Reservation.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .scheduleId(request.getScheduleId())
                .status("WAITING")
                .reservationTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        Reservation savedReservation =
                reservationRepository.save(reservation);

        Map<String, Object> message = new HashMap<>();
        message.put("reservationId", savedReservation.getId());
        message.put("patientId", savedReservation.getPatientId());
        message.put("doctorId", savedReservation.getDoctorId());
        message.put("status", savedReservation.getStatus());
        message.put(
                "reservationTime",
                savedReservation.getReservationTime().toString()
        );

        rabbitTemplate.convertAndSend(
                "hospital.exchange",
                "booking.notification",
                message
        );

        System.out.println("[예약 생성 완료]");
        System.out.println("[RabbitMQ 발행 성공] " + message);

        return savedReservation;
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

        reservation.setStatus("CONFIRMED");

        Reservation savedReservation =
                reservationRepository.save(reservation);

        Map<String, Object> message = new HashMap<>();
        message.put("reservationId", savedReservation.getId());
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

        reservation.setStatus("CANCELED");

        Reservation savedReservation =
                reservationRepository.save(reservation);

        Map<String, Object> message = new HashMap<>();
        message.put("reservationId", savedReservation.getId());
        message.put("status", savedReservation.getStatus());

        rabbitTemplate.convertAndSend(
                "hospital.exchange",
                "booking.notification",
                message
        );

        return savedReservation;
    }
}