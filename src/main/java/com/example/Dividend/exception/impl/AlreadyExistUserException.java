package com.example.Dividend.exception.impl;

import com.example.Dividend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class AlreadyExistUserException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMassage() {
        return "이미 존재하는 사용자명입니다.";
    }
}