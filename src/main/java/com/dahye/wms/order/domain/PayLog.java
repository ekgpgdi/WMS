package com.dahye.wms.order.domain;

import com.dahye.wms.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pay_log")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(length = 50)
    private String payStatus;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(length = 50)
    private String transactionId;

    @Column(length = 256)
    private String message;
}

