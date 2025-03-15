package com.raul.forumhub.topic.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.raul.forumhub.topic.exception.BusinessException;
import com.raul.forumhub.topic.exception.InstanceNotFoundException;
import com.raul.forumhub.topic.exception.RestClientException;
import com.raul.forumhub.topic.exception.ValidationException;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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
                "Falha de validação", detail, request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    private ResponseEntity<ExceptionEntity> constraintViolationExceptionResolver(jakarta.validation.ConstraintViolationException ex,
                                                                                 HttpServletRequest request) {
        String detail = ex.getConstraintViolations().stream().findFirst().orElseThrow().getMessageTemplate();
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Falha de validação", detail, request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    private ResponseEntity<ExceptionEntity> propertyPathExceptionResolver(PropertyReferenceException ex, HttpServletRequest request) {
        String detail = String.format("A propriedade '%s' não existe", ex.getPropertyName());
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Falha de validação", detail, request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    private ResponseEntity<ExceptionEntity> dataAccessExceptionResolver(DataAccessException ex, HttpServletRequest request) {
        if (ex.getCause() instanceof ConstraintViolationException && ((ConstraintViolationException) ex.getCause())
                .getSQLException().getSQLState().equals("23505")) {
            return new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.CONFLICT.value(),
                    "Solicitação não processada", "Payload conflitante", request.getRequestURI()),
                    headers(), HttpStatus.CONFLICT);
        } else if ((ex.getCause() instanceof DataException && ((DataException) ex.getCause()).getSQLException().getSQLState().equals("22001"))) {
            return new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.PAYLOAD_TOO_LARGE.value(),
                    "Solicitação não processada", "Payload com valor muito grande", request.getRequestURI()),
                    headers(), HttpStatus.PAYLOAD_TOO_LARGE);
        } else if (ex.getCause() instanceof ConstraintViolationException && ((ConstraintViolationException) ex.getCause())
                .getSQLException().getSQLState().equals("23503")) {
            return new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.CONFLICT.value(),
                    "Solicitação não processada", "O curso informado está associado a um tópico", request.getRequestURI()),
                    headers(), HttpStatus.CONFLICT);
        }
        return notExpectedExceptionResolver(ex, request);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    private ResponseEntity<ExceptionEntity> illegalArgExceptionResolver(RuntimeException ex, HttpServletRequest request) {
        return ex instanceof IllegalArgumentException ?
                new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                        "Solicitação não processada", ex.getMessage(), request.getRequestURI()), headers(), HttpStatus.BAD_REQUEST) :
                ex instanceof MethodArgumentTypeMismatchException ?
                        new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                                "Solicitação não processada", ex.getMessage(), request.getRequestURI()), headers(), HttpStatus.BAD_REQUEST) :
                        new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                                "Solicitação não processada", "Solicitação com valor ilegível", request.getRequestURI()),
                                headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    private ResponseEntity<ExceptionEntity> notReadableExceptionResolver(HttpMessageNotReadableException ex, HttpServletRequest request) {
        InvalidFormatException invalidEx = (InvalidFormatException) ex.getCause();
        return new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Solicitação não processada", String.format("A propriedade '%s' é inválida", invalidEx.getValue()), request.getRequestURI()),
                headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RestClientException.class)
    private ResponseEntity<ExceptionEntity> restClientExceptionResolver(RestClientException ex, HttpServletRequest request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), ex.getHttpStatusCode().value(),
                "Solicitação não processada", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), ex.getHttpStatusCode());
    }

    @ExceptionHandler(BusinessException.class)
    private ResponseEntity<ExceptionEntity> businessServiceExceptionResolver(BusinessException ex, HttpServletRequest request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Erro de business", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ValidationException.class)
    private ResponseEntity<ExceptionEntity> validationExceptionResolver(ValidationException ex, HttpServletRequest request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.I_AM_A_TEAPOT.value(),
                "Falha durante as validações", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.I_AM_A_TEAPOT);
    }

    @ExceptionHandler(InstanceNotFoundException.class)
    private ResponseEntity<ExceptionEntity> instanceNotFoundExceptionResolver(InstanceNotFoundException ex, HttpServletRequest request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                "Solicitação não encontrada", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServletException.class)
    private ResponseEntity<ExceptionEntity> noResourceExceptionResolver(ServletException ex, HttpServletRequest request) {
        HttpStatus status = ex instanceof NoResourceFoundException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), status.value(),
                "Solicitação não processada", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), status);
    }


    @ExceptionHandler(AuthorizationDeniedException.class)
    private ResponseEntity<?> authorizationDeniedExceptionResolver(Authentication authentication) {
        HttpStatus status = authentication == null ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        return new ResponseEntity<>(headers(), status);
    }


    @ExceptionHandler(Exception.class)
    private ResponseEntity<ExceptionEntity> notExpectedExceptionResolver(Exception ex, HttpServletRequest request) {
        log.error("Falha inesperada: {}", ex.getMessage());
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Solicitação não processada", "Erro inesperado no servidor", request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
