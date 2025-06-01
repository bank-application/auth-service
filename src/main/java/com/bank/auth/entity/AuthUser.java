package com.bank.auth.entity;

import com.bank.auth.enums.Role;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "auth_user")
public class AuthUser implements Serializable {
    private String id;
    private String email;
    private String mobNumber;
    private String password;
    private List<Role> role;
    private String createdAt;
    private String updatedAt;
}
