package com.takarub.springJWT.exception;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorResponse {

    private int status;
    private boolean success;

    private Error error;

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Error{
        private String code;

        private String message;
    }
}
