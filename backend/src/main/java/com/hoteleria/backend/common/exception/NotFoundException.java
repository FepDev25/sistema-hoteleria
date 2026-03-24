package com.hoteleria.backend.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessException {

    public NotFoundException(String code, String message) {
        super(code, message, HttpStatus.NOT_FOUND);
    }
}
