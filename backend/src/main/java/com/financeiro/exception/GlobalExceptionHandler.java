package com.financeiro.exception;

import com.financeiro.dto.MessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Handler global de exceções para a aplicação.
 * Intercepta exceções lançadas pelos controllers e retorna respostas HTTP apropriadas.
 * 
 * Requirements: 1.5, 2.5, 2.6, 4.5, 10.1, 10.2, 10.3, 10.4, 10.5
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Trata erros de autenticação com Google Sheets API.
     * Retorna HTTP 401 Unauthorized.
     */
    @ExceptionHandler(GoogleSheetsAuthException.class)
    public ResponseEntity<MessageDTO> handleAuthError(GoogleSheetsAuthException ex) {
        logger.error("Authentication error: {}", ex.getMessage(), ex);
        MessageDTO response = new MessageDTO("Falha na autenticação com Google Sheets", "error");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Trata erros de validação de dados.
     * Retorna HTTP 400 Bad Request.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<MessageDTO> handleValidationError(ValidationException ex) {
        logger.warn("Validation error: {}", ex.getMessage());
        MessageDTO response = new MessageDTO(ex.getMessage(), "error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Trata erros de recurso não encontrado.
     * Retorna HTTP 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageDTO> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        MessageDTO response = new MessageDTO(ex.getMessage(), "error");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Trata exceções genéricas não tratadas especificamente.
     * Retorna HTTP 500 Internal Server Error.
     * Stack traces são registrados no log mas não expostos ao cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageDTO> handleGenericError(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        MessageDTO response = new MessageDTO("Erro interno do servidor", "error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
