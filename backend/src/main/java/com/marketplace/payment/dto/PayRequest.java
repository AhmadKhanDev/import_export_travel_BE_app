package com.marketplace.payment.dto;

import com.marketplace.payment.entity.PaymentProviderType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRequest {

    private PaymentProviderType paymentProvider;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}