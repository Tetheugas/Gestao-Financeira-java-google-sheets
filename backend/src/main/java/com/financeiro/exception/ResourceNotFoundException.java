package com.financeiro.exception;

/**
 * Exceção lançada quando recursos não são encontrados (ex: aba ou mês inexistente).
 * Relacionada aos requisitos 10.1, 10.2
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
