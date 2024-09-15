package com.takarub.springJWT.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "token")
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    @Id
    @GeneratedValue
    private Integer id;

    private String token;

    @Column(name = "is_logged_out")
    private boolean isLoggedOut;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
