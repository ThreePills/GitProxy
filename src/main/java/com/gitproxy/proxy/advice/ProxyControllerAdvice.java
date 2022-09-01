package com.gitproxy.proxy.advice;

import com.gitproxy.proxy.exception.GitUserNotFoundException;
import com.gitproxy.proxy.response.FailResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ProxyControllerAdvice {

    @ExceptionHandler(GitUserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<FailResponse> handleGitUserNotFoundException() {
        FailResponse failResponse = new FailResponse();
        failResponse.setStatus(HttpStatus.NOT_FOUND.value());
        failResponse.setMessage(HttpStatus.NOT_FOUND.getReasonPhrase());
        return new ResponseEntity<>(failResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseEntity<FailResponse> handleHttpMediaTypeNotSupportedException() {
        FailResponse failResponse = new FailResponse();
        failResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
        failResponse.setMessage("Unsupported Media Type");
        return new ResponseEntity<>(failResponse, HttpStatus.NOT_ACCEPTABLE);
    }
}
