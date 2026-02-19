package com.financeiro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para requisições de cadastro de gastos.
 * Contém os dados necessários para criar um novo gasto na planilha.
 * 
 * Requirements: 2.1, 4.3
 */
public class ExpenseRequestDTO {
    
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;
    
    @NotNull(message = "Valor é obrigatório")
    private Double valor;
    
    @NotBlank(message = "Aba é obrigatória")
    private String aba;
    
    @NotBlank(message = "Mês é obrigatório")
    private String mes;
    
    public ExpenseRequestDTO() {
    }
    
    public ExpenseRequestDTO(String descricao, Double valor, String aba, String mes) {
        this.descricao = descricao;
        this.valor = valor;
        this.aba = aba;
        this.mes = mes;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public Double getValor() {
        return valor;
    }
    
    public void setValor(Double valor) {
        this.valor = valor;
    }
    
    public String getAba() {
        return aba;
    }
    
    public void setAba(String aba) {
        this.aba = aba;
    }
    
    public String getMes() {
        return mes;
    }
    
    public void setMes(String mes) {
        this.mes = mes;
    }
}
