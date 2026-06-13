package com.hospital.booking.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;

    private Long doctorId;

    @Column(length = 50)
    private String patientName;

    @Column(length = 50)
    private String doctorName;

    private Long scheduleId;

    private Integer amount;

    @Column(length = 20)
    private String status;

    // 확정(CONFIRMED) 받은 예약을 고객 본인이 취소한 경우 true. 어드민의 WAITING 거절 취소와 구분하기 위함.
    private Boolean selfCanceled;

    private LocalDateTime reservationTime;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public void updateStatus(String status) {
        this.status = status;
    }

    public void markSelfCanceled() {
        this.selfCanceled = true;
    }
}