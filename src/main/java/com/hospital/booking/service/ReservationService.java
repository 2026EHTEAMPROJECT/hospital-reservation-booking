package com.hospital.booking.service;

import com.hospital.booking.domain.Reservation;
import com.hospital.booking.dto.CreateReservationRequest;
import com.hospital.booking.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
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

        Reservation savedReservation = reservationRepository.save(reservation);

        Map<String, Object> message = new HashMap<>();
        message.put("reservationId", savedReservation.getId());
        message.put("patientId", savedReservation.getPatientId());
        message.put("doctorId", savedReservation.getDoctorId());
        message.put("status", savedReservation.getStatus());
        message.put("reservationTime", savedReservation.getReservationTime().toString());
        message.put("patientName", "홍길동");
        message.put("doctorName", "김의사");

        rabbitTemplate.convertAndSend(
                "hospital.exchange",
                "booking.notification",
                message
        );

        System.out.println("[예약 생성 완료]");
        System.out.println("[RabbitMQ 발행 성공] " + message);

        return savedReservation;
    }
}