package com.bank.auth.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "token")
public class Token implements Serializable {
    @Id
    private String id;
    private String subject;
    private String token;
    private String issuedAt;
    private String expiresAt;
    private boolean revoked;
}
