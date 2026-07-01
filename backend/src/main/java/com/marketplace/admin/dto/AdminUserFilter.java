package com.marketplace.admin.dto;

import com.marketplace.user.entity.AccountStatus;
import com.marketplace.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserFilter {

    private Role role;
    private AccountStatus accountStatus;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Boolean profileCompleted;
}
