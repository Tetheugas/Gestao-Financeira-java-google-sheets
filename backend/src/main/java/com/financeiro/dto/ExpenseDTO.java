package com.financeiro.dto;

/**
 * DTO para respostas da API contendo dados de gastos.
 * Representa um gasto com descrição e valor já formatado em BRL.
 * 
 * Requirements: 1.3, 7.2
 */
public class ExpenseDTO {
    
    private int rowId;
    private String descricao;
    private String valorFormatado;
    private Double valor;
    
    public ExpenseDTO() {
    }
    
    public ExpenseDTO(String descricao, String valorFormatado) {
        this.descricao = descricao;
        this.valorFormatado = valorFormatado;
    }

    public ExpenseDTO(int rowId, String descricao, String valorFormatado, Double valor) {
        this.rowId = rowId;
        this.descricao = descricao;
        this.valorFormatado = valorFormatado;
        this.valor = valor;
    }
    
    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
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

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
}
