package com.raul.forumhub.topic.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Locale;

@RestControllerAdvice
public class ExceptionResponseHandler {

    public HttpHeaders headers(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        headers.setContentLanguage(new Locale("Pt", "BR"));
        return headers;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<Object> constraintExceptionResolver(ConstraintViolationException ex, HttpServletRequest request){
        String detail = ex.getConstraintViolations().stream().map(ConstraintViolation::getMessageTemplate)
                .findFirst().orElse("Erro de violação de restrição");
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "erro de restrição", detail, request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<Object> duplicatedDataExceptionResolver(DataIntegrityViolationException ex, HttpServletRequest request){
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "erro de restrição", "registro já existente", request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    private ResponseEntity<Object> notReadableExceptionResolver(HttpMessageNotReadableException ex, HttpServletRequest request){
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "solicitação desconhecida", "solicitação com algum valor não legível", request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

}
