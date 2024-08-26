package com.raul.forumhub.user.exception.handler;

import com.raul.forumhub.user.exception.InstanceNotFoundException;
import com.raul.forumhub.user.exception.MalFormatedParamUserException;
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
    private ResponseEntity<ExceptionEntity> constraintExceptionResolver(ConstraintViolationException ex, HttpServletRequest request){
        String detail = ex.getConstraintViolations().stream().map(ConstraintViolation::getMessageTemplate)
                .findFirst().orElse("Erro de violação de restrição");
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Erro de restrição", detail, request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<ExceptionEntity> duplicatedDataExceptionResolver(DataIntegrityViolationException ex, HttpServletRequest request){
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.CONFLICT.value(),
                "Erro de restrição", "Registro já existente", request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    private ResponseEntity<ExceptionEntity> notReadableExceptionResolver(HttpMessageNotReadableException ex, HttpServletRequest request){
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Solicitação desconhecida", "Solicitação com valor ilegível", request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(InstanceNotFoundException.class)
    public ResponseEntity<ExceptionEntity> instanceNotFoundExceptionResolver(InstanceNotFoundException ex, HttpServletRequest request){
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                "Solicitação não encontrada", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MalFormatedParamUserException.class)
    private ResponseEntity<ExceptionEntity> malFormatedParamExceptionResolver(MalFormatedParamUserException ex, HttpServletRequest request){
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Solicitação não esperada", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

}
