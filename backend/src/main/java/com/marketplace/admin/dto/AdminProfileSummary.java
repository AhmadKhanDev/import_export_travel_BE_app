package com.marketplace.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileSummary {

    private String fullName;
    private String phoneNumber;
    private boolean profileCompleted;
}
