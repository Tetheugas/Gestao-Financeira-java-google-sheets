package com.financeiro.dto;

/**
 * DTO para respostas de mensagens de erro ou sucesso.
 * Usado para comunicar o resultado de operações ao cliente.
 * 
 * Requirements: 4.5, 10.1
 */
public class MessageDTO {
    
    private String message;
    private String status;
    
    public MessageDTO() {
    }
    
    public MessageDTO(String message, String status) {
        this.message = message;
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
