package com.example.spotorder.global.presentation.code;

import org.springframework.http.HttpStatus;

public interface BaseSuccessCode {
    
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
