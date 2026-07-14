package com.thabat.identity.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class ValidDateOfBirthValidator implements ConstraintValidator<ValidDateOfBirth, LocalDate> {

    private static final int MIN_AGE = 5;
    private static final int MAX_AGE = 120;

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
        if (dateOfBirth == null) {
            return true;
        }

        LocalDate today = LocalDate.now();
        int age = Period.between(dateOfBirth, today).getYears();

        if (age < MIN_AGE) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "User must be at least 5 years old"
            ).addConstraintViolation();
            return false;
        }

        if (age > MAX_AGE) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "User cannot be older than 120 years"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
