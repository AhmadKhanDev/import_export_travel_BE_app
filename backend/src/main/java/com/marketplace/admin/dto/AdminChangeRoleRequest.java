package com.marketplace.admin.dto;

import com.marketplace.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangeRoleRequest {

    @NotNull(message = "role is required")
    private Role role;
}
