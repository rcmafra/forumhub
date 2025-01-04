package com.raul.forumhub.user.security.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = PasswordConstraintValidator.class)
public @interface PasswordRules {

    String message() default "Falha na validação de políticas de senha";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
