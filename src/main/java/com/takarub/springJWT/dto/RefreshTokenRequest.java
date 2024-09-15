package com.takarub.springJWT.dto;

import lombok.Data;

@Data
public class RefreshTokenRequest {

    private String refreshToken;
}
