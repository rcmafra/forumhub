package com.raul.forumhub.user.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.raul.forumhub.user.exception.InstanceNotFoundException;
import com.raul.forumhub.user.exception.MalFormatedParamUserException;
import com.raul.forumhub.user.exception.PasswordRulesException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
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

    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    private ResponseEntity<ExceptionEntity> paramValidationExceptionResolver(Exception ex, HttpServletRequest request) {
        String detail = ex instanceof MethodArgumentNotValidException argEx ?
                argEx.getBindingResult().getAllErrors().stream().findFirst().orElseThrow().getDefaultMessage() :
                ex.getCause().getMessage();
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
                            headers(), HttpStatus.CONFLICT) : this.notExpectedExceptionResolver(constraintViolationException, request);
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
    private ResponseEntity<ExceptionEntity> notReadableExceptionResolver(HttpMessageNotReadableException ex, HttpServletRequest request) {
        InvalidFormatException invalidEx = (InvalidFormatException) ex.getCause();
        return new ResponseEntity<>(new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                DEFAULT_VALIDATION_TITLE, String.format("A propriedade '%s' enviada é inválida", invalidEx.getValue()), request.getRequestURI()),
                headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InstanceNotFoundException.class)
    public ResponseEntity<ExceptionEntity> instanceNotFoundExceptionResolver(InstanceNotFoundException ex, HttpServletRequest request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MalFormatedParamUserException.class)
    private ResponseEntity<ExceptionEntity> malFormatedParamExceptionResolver(MalFormatedParamUserException ex, HttpServletRequest request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                DEFAULT_VALIDATION_TITLE, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServletException.class)
    private ResponseEntity<ExceptionEntity> noResourceExceptionResolver(ServletException ex, HttpServletRequest request) {
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

    @ExceptionHandler(PasswordRulesException.class)
    private ResponseEntity<ExceptionEntity> passwordRulesValidationExceptionResolver(PasswordRulesException ex, HttpServletRequest request) {
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Falha na validação da password", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), entity.status());
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<ExceptionEntity> notExpectedExceptionResolver(Exception ex, HttpServletRequest request) {
        log.error("Falha inesperada: {} - causa: {}", ex.getMessage(), ex.getCause(), ex);
        ExceptionEntity entity = new ExceptionEntity(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Falha inesperada", "Erro inesperado no servidor.", request.getRequestURI());
        return new ResponseEntity<>(entity, headers(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
