package com.takarub.springJWT.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException ex) {
//        return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
//    }

    // You can add more exception handlers for other exceptions here
    //AccessDeniedException


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<ErrorResponse> handleBadCredentialsException1(BadCredentialsException ee){
//
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(ErrorResponse
//                        .builder()
//                        .status(HttpStatus.BAD_REQUEST.value())
//                        .error(ErrorResponse.Error.builder().code(ee.getMessage())
//                                .message(ee.getMessage()).build())
//                        .build()
//                );
//    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse
                        .builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .success(false)
                        .error(ErrorResponse.Error.builder()
                                .code("BAD_CREDENTIALS")
                                .message(ex.getMessage())
                                .build())
                        .build()
                );
    }

    @ExceptionHandler(RequestRejectedException.class)
    public ResponseEntity<ErrorResponse> handleRequestRejectedException(RequestRejectedException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse
                        .builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .error(ErrorResponse.Error.builder()
                                .code("REQUEST_REJECTED")
                                .message(ex.getMessage())
                                .build())
                        .build()
                );
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> AccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse
                        .builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .error(ErrorResponse.Error.builder()
                                .code("REQUEST_REJECTED")
                                .message(ex.getMessage())
                                .build())
                        .build()
                );
    }


}
