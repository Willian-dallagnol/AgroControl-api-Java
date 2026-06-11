package dev.willian.agrocontrol.exception;

/**
 * Lancada quando um recurso nao existe OU nao pertence ao usuario autenticado.
 * Em ambos os casos respondemos 404 para nao vazar a existencia de dados de terceiros.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super("%s nao encontrado(a) para o identificador: %s".formatted(resource, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
