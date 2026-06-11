package dev.willian.agrocontrol.validation;

import dev.willian.agrocontrol.dto.crop.CropRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HarvestWindowValidator implements ConstraintValidator<ValidHarvestWindow, CropRequest> {

    @Override
    public boolean isValid(CropRequest request, ConstraintValidatorContext context) {
        if (request == null || request.plantingDate() == null || request.harvestDate() == null) {
            return true; // datas opcionais: nada a validar
        }
        boolean valid = !request.harvestDate().isBefore(request.plantingDate());
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("harvestDate")
                    .addConstraintViolation();
        }
        return valid;
    }
}
