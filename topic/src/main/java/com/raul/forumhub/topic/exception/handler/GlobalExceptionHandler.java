package com.raul.forumhub.topic.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.raul.forumhub.topic.exception.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String DEFAULT_VALIDATION_TITLE = "Falha de validação";

    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        headers.setContentLanguage(new Locale("pt", "br"));
        return headers;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<ExceptionEntity> paramValidationExceptionResolver(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String detail = ex.getBindingResult().getAllErrors().stream().findFirst().orElseThrow().getDefaultMessage();
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                DEFAULT_VALIDATION_TITLE, detail, request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    private ResponseEntity<ExceptionEntity> constraintViolationExceptionResolver(jakarta.validation.ConstraintViolationException ex,
                                                                                 HttpServletRequest request) {
        String detail = ex.getConstraintViolations().stream().findFirst().orElseThrow().getMessageTemplate();
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                DEFAULT_VALIDATION_TITLE, detail, request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    private ResponseEntity<ExceptionEntity> propertyPathExceptionResolver(PropertyReferenceException ex, HttpServletRequest request) {
        String detail = String.format("A propriedade '%s' enviada não existe", ex.getPropertyName());
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                DEFAULT_VALIDATION_TITLE, detail, request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    private ResponseEntity<ExceptionEntity> missingRequestParamExceptionResolver(MissingServletRequestParameterException ex,
                                                                                 HttpServletRequest request) {
        String detail = String.format("A propriedade '%s' não foi informada", ex.getParameterName());
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                DEFAULT_VALIDATION_TITLE, detail, request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    private ResponseEntity<ExceptionEntity> dataAccessExceptionResolver(DataAccessException ex, HttpServletRequest request) {
        ResponseEntity<ExceptionEntity> responseEntity = null;
        if (ex.getCause() instanceof ConstraintViolationException constraintViolationException) {
            responseEntity = constraintViolationException.getSQLException().getSQLState().equals("23505") ?
                    new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.CONFLICT.value(),
                            "Falha de restrição", "Payload conflitante com outro registro", request.getRequestURI()),
                            headers(), HttpStatus.CONFLICT) :
                    constraintViolationException.getSQLException().getSQLState().equals("23503") ?
                            new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.CONFLICT.value(),
                                    "Falha de restrição", "O curso informado não pode ser removido porque está associado a um tópico",
                                    request.getRequestURI()), headers(), HttpStatus.CONFLICT) :
                            this.notExpectedExceptionResolver(constraintViolationException, request);
        } else if (ex.getCause() instanceof DataException dataException) {
            responseEntity = dataException.getSQLException().getSQLState().equals("22001") ?
                    new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.PAYLOAD_TOO_LARGE.value(),
                            DEFAULT_VALIDATION_TITLE, "Payload com valor muito grande", request.getRequestURI()),
                            headers(), HttpStatus.PAYLOAD_TOO_LARGE) : this.notExpectedExceptionResolver(dataException, request);
        }
        return responseEntity;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    private ResponseEntity<ExceptionEntity> illegalArgExceptionResolver(RuntimeException ex, HttpServletRequest request) {
        return ex instanceof MethodArgumentTypeMismatchException mismatchException ?
                new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                        DEFAULT_VALIDATION_TITLE, String.format("O valor '%s' enviado é inválido", mismatchException.getValue()),
                        request.getRequestURI()), headers(), HttpStatus.BAD_REQUEST) : this.notExpectedExceptionResolver(ex, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    private ResponseEntity<ExceptionEntity> notReadableExceptionResolver(HttpMessageNotReadableException
                                                                                 ex, HttpServletRequest request) {
        InvalidFormatException invalidEx = (InvalidFormatException) ex.getCause();
        return new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                DEFAULT_VALIDATION_TITLE, String.format("A propriedade '%s' enviada é inválida", invalidEx.getValue()), request.getRequestURI()),
                headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RestClientException.class)
    private ResponseEntity<ExceptionEntity> restClientExceptionResolver(RestClientException ex, HttpServletRequest
            request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), ex.getHttpStatusCode().value(),
                "Falha no serviço de usuário", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), ex.getHttpStatusCode());
    }

    @ExceptionHandler(BusinessException.class)
    private ResponseEntity<ExceptionEntity> businessServiceExceptionResolver(BusinessException
                                                                                     ex, HttpServletRequest request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Erro de business", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ValidationException.class)
    private ResponseEntity<ExceptionEntity> validationExceptionResolver(ValidationException ex, HttpServletRequest
            request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.I_AM_A_TEAPOT.value(),
                DEFAULT_VALIDATION_TITLE, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.I_AM_A_TEAPOT);
    }

    @ExceptionHandler(PrivilegeValidationException.class)
    private ResponseEntity<ExceptionEntity> privilegeValidationException(PrivilegeValidationException
                                                                                ex, HttpServletRequest request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.I_AM_A_TEAPOT.value(),
                "Previlégio insuficiente", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.I_AM_A_TEAPOT);
    }

    @ExceptionHandler(InstanceNotFoundException.class)
    private ResponseEntity<ExceptionEntity> instanceNotFoundExceptionResolver(InstanceNotFoundException
                                                                                      ex, HttpServletRequest request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServletException.class)
    private ResponseEntity<ExceptionEntity> noResourceExceptionResolver(ServletException ex, HttpServletRequest
            request) {
        HttpStatus status = ex instanceof NoResourceFoundException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), status.value(),
                "Falha no processamento", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), status);
    }


    @ExceptionHandler(AuthorizationDeniedException.class)
    private ResponseEntity<?> authorizationDeniedExceptionResolver(Authentication authentication) {
        HttpStatus status = authentication == null ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        return new ResponseEntity<>(headers(), status);
    }


    @ExceptionHandler(Exception.class)
    private ResponseEntity<ExceptionEntity> notExpectedExceptionResolver(Exception ex, HttpServletRequest request) {
        log.error("Falha inesperada: {} - causa: {}", ex.getMessage(), ex.getCause(), ex);
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Falha inesperada", "Erro inesperado no servidor. Mais detalhes no log.", request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
