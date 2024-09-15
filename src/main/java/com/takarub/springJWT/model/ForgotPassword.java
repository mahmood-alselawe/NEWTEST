package com.takarub.springJWT.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class ForgotPassword {

    @Id
    @GeneratedValue
    private Integer fpId;

    @Column(nullable = false)
    private Integer otp;

    @Column(nullable = false)
    private Date exirationDate;

    @OneToOne
    private User user;
}
