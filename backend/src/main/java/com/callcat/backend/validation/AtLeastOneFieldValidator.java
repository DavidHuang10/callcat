package com.callcat.backend.validation;

import com.callcat.backend.dto.UpdateCallRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AtLeastOneFieldValidator implements ConstraintValidator<AtLeastOneField, UpdateCallRequest> {

    @Override
    public void initialize(AtLeastOneField constraintAnnotation) {
    }

    @Override
    public boolean isValid(UpdateCallRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return false;
        }

        return request.getCalleeName() != null ||
               request.getPhoneNumber() != null ||
               request.getSubject() != null ||
               request.getPrompt() != null ||
               request.getScheduledFor() != null ||
               request.getAiLanguage() != null ||
               request.getVoiceId() != null;
    }
}