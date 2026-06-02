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

    public static final String PAYMENT_REQUEST_KEY =
            "payment.request";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}