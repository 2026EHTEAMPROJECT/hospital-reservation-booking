package com.hospital.booking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {

    public static final String EXCHANGE = "hospital.exchange";

    public static final String BOOKING_NOTIFICATION_QUEUE = "booking.notification.queue";
    public static final String BOOKING_NOTIFICATION_KEY   = "booking.notification";

    public static final String PAYMENT_REQUEST_QUEUE = "booking.payment.queue";
    public static final String PAYMENT_REQUEST_KEY   = "booking.payment";

    @Bean
    DirectExchange hospitalExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue bookingNotificationQueue() {
        return new Queue(BOOKING_NOTIFICATION_QUEUE, true);
    }

    @Bean
    Queue paymentRequestQueue() {
        return new Queue(PAYMENT_REQUEST_QUEUE, true);
    }

    @Bean
    Binding bookingNotificationBinding(Queue bookingNotificationQueue, DirectExchange hospitalExchange) {
        return BindingBuilder.bind(bookingNotificationQueue).to(hospitalExchange).with(BOOKING_NOTIFICATION_KEY);
    }

    @Bean
    Binding paymentRequestBinding(Queue paymentRequestQueue, DirectExchange hospitalExchange) {
        return BindingBuilder.bind(paymentRequestQueue).to(hospitalExchange).with(PAYMENT_REQUEST_KEY);
    }

    @Bean
    Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
