package com.hospital.booking.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "hospital.exchange";

    public static final String BOOKING_NOTIFICATION_KEY =
            "booking.notification";

    // payment-service 의 결제요청 큐(booking.payment.queue) 바인딩 키와 일치시켜야 한다.
    public static final String PAYMENT_REQUEST_KEY =
            "booking.payment";

    // 예약 취소 시 payment-service 에 환불을 요청하는 라우팅 키.
    public static final String PAYMENT_REFUND_KEY =
            "payment.refund";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}