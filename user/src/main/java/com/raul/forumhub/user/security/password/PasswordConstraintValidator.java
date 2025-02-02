package com.raul.forumhub.user.security.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Properties;

public class PasswordConstraintValidator implements ConstraintValidator<PasswordRules, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        Assert.notNull(password, "A senha não pode ser vazia");

        PasswordValidator passwordValidator = new PasswordValidator(translatedPasswordRules(),
                new LengthRule(8, 16),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 5, false),
                new IllegalSequenceRule(EnglishSequenceData.Numerical, 5, false),
                new IllegalSequenceRule(EnglishSequenceData.USQwerty, 5, false),
                new WhitespaceRule());

        RuleResult result = passwordValidator.validate(new PasswordData(password));

        if (result.isValid()) {
            return true;

        }

        context.buildConstraintViolationWithTemplate(passwordValidator.getMessages(result)
                .stream().findFirst().orElseThrow()).addConstraintViolation().disableDefaultConstraintViolation();

        return false;
    }

    private MessageResolver translatedPasswordRules() {
        Properties properties = new Properties();
        try {
            ClassPathResource resource = new ClassPathResource("translated-passwd-rule-msg.properties");
            properties.load(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new PropertiesMessageResolver(properties);

    }

}
