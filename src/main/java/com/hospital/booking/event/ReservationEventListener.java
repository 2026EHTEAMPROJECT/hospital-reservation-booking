package com.hospital.booking.event;

import com.hospital.booking.config.RabbitConfig;
import com.hospital.booking.dto.BookingNotificationMessage;
import com.hospital.booking.dto.PaymentRequestMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// DB 커밋 이후에만 메시지를 발행하여 트랜잭션-메시지 불일치를 방지한다.
@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationCreated(ReservationCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.BOOKING_NOTIFICATION_KEY,
                new BookingNotificationMessage(
                        event.reservationId(),
                        event.patientId(),
                        event.doctorId(),
                        event.status(),
                        event.reservationTime().toString(),
                        event.patientName(),
                        event.doctorName()
                )
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.PAYMENT_REQUEST_KEY,
                new PaymentRequestMessage(
                        event.reservationId(),
                        event.patientId(),
                        event.amount(),
                        event.patientName()
                )
        );
    }
}
