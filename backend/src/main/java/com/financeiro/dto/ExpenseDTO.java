package com.financeiro.dto;

/**
 * DTO para respostas da API contendo dados de gastos.
 * Representa um gasto com descrição e valor já formatado em BRL.
 * 
 * Requirements: 1.3, 7.2
 */
public class ExpenseDTO {
    
    private String descricao;
    private String valorFormatado;
    
    public ExpenseDTO() {
    }
    
    public ExpenseDTO(String descricao, String valorFormatado) {
        this.descricao = descricao;
        this.valorFormatado = valorFormatado;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getValorFormatado() {
        return valorFormatado;
    }
    
    public void setValorFormatado(String valorFormatado) {
        this.valorFormatado = valorFormatado;
    }
}
