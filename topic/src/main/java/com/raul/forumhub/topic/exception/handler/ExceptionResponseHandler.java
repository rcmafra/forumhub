package com.raul.forumhub.topic.exception.handler;

import com.raul.forumhub.topic.exception.AbstractServiceException;
import com.raul.forumhub.topic.exception.InstanceNotFoundException;
import com.raul.forumhub.topic.exception.RestClientException;
import com.raul.forumhub.topic.exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.exception.GenericJDBCException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Locale;

@RestControllerAdvice
public class ExceptionResponseHandler {

    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        headers.setContentLanguage(new Locale("Pt", "BR"));
        return headers;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<ExceptionEntity> constraintExceptionResolver(ConstraintViolationException ex, HttpServletRequest request) {
        String detail = ex.getConstraintViolations().stream().map(ConstraintViolation::getMessageTemplate)
                .findFirst().orElse("Erro de violação de constraint");
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Erro de constraint", detail, request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionException.class)
    private ResponseEntity<ExceptionEntity> transactionExceptionResolver(TransactionException ex, HttpServletRequest request) {
        return ex.getCause().getCause() instanceof ConstraintViolationException ?
                this.constraintExceptionResolver((ConstraintViolationException) ex.getCause().getCause(), request) :
                this.notExpectedExceptionResolver(ex, request);
    }

    @ExceptionHandler(DataAccessException.class)
    private ResponseEntity<ExceptionEntity> dataAccessExceptionResolver(DataAccessException ex, HttpServletRequest request) {
        return ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException ?
                new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.CONFLICT.value(),
                        "Solicitação não processada", "Payload conflitante", request.getRequestURI()),
                        headers(), HttpStatus.CONFLICT) :
                ex.getCause() instanceof GenericJDBCException ?
                        new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                                "Solicitação não processada", "Payload com valor muito grande", request.getRequestURI()),
                                headers(), HttpStatus.BAD_REQUEST) : this.notExpectedExceptionResolver(ex, request);

    }

    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class})
    private ResponseEntity<ExceptionEntity> notReadableExceptionResolver(RuntimeException ex, HttpServletRequest request) {
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

    @ExceptionHandler(RestClientException.class)
    private ResponseEntity<ExceptionEntity> restClientExceptionResolver(RestClientException ex, HttpServletRequest request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), ex.getHttpStatusCode().value(),
                "Solicitação não processada", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), ex.getHttpStatusCode());
    }

    @ExceptionHandler(AbstractServiceException.class)
    private ResponseEntity<ExceptionEntity> businessServiceExceptionResolver(AbstractServiceException ex, HttpServletRequest request) {
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

    @ExceptionHandler({ServletException.class, NoResourceFoundException.class})
    private ResponseEntity<ExceptionEntity> servletExceptionResolver(ServletException ex, HttpServletRequest request) {
        HttpStatus status = ex.getCause() instanceof NoResourceFoundException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), status.value(),
                "Solicitação não processada", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), status);
    }


    @ExceptionHandler(AuthorizationDeniedException.class)
    private ResponseEntity<ExceptionEntity> authorizationDeniedExceptionResolver(AccessDeniedException ex, HttpServletRequest request, Authentication
            authentication) {
        HttpStatus status = authentication == null ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        return new ResponseEntity<>(headers(), status);
    }


    @ExceptionHandler(Exception.class)
    private ResponseEntity<ExceptionEntity> notExpectedExceptionResolver(Exception ex, HttpServletRequest request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Solicitação não processada", "Erro inesperado no servidor", request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
