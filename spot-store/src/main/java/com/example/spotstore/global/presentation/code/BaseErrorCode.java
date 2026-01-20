package com.example.spotstore.global.presentation.code;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
    
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
