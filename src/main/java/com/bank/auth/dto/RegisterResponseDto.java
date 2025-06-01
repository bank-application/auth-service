package com.bank.auth.dto;

import com.bank.auth.enums.Role;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponseDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobNumber;
    private String password;
    private List<Role> role;
}
