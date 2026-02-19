package com.financeiro.exception;

/**
 * Exceção lançada quando ocorrem erros de validação de dados.
 * Relacionada aos requisitos 2.5, 2.6, 10.1, 10.2
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
