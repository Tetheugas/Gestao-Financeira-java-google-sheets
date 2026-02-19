package com.financeiro.exception;

/**
 * Exceção lançada quando ocorrem erros de autenticação com Google Sheets API.
 * Relacionada aos requisitos 1.5, 10.1, 10.2
 */
public class GoogleSheetsAuthException extends RuntimeException {
    
    public GoogleSheetsAuthException(String message) {
        super(message);
    }
    
    public GoogleSheetsAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
