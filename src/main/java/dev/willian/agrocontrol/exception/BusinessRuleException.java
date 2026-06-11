package dev.willian.agrocontrol.exception;

/**
 * Violacao de regra de negocio (ex.: soma das areas dos talhoes excede a area da fazenda).
 * Mapeada para HTTP 422 (Unprocessable Entity).
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
