package com.finance.dto.request;

import com.finance.enums.Role;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String name;

    private Role role;

    private Boolean isActive;
}
