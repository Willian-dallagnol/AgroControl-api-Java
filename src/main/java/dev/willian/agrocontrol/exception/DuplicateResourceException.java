package dev.willian.agrocontrol.exception;

/**
 * Tentativa de criar recurso que viola unicidade (ex.: e-mail ja cadastrado).
 * Mapeada para HTTP 409 (Conflict).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
