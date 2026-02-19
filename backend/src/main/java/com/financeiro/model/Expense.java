package com.financeiro.model;

/**
 * Representa um gasto financeiro com descrição e valor.
 * Requirements: 1.1, 2.1
 */
public class Expense {
    
    private int rowId;
    private String descricao;
    private Double valor;
    
    /**
     * Construtor padrão.
     */
    public Expense() {
    }
    
    /**
     * Construtor com todos os campos (para leitura).
     *
     * @param rowId Índice da linha na planilha
     * @param descricao Descrição do gasto
     * @param valor Valor monetário do gasto
     */
    public Expense(int rowId, String descricao, Double valor) {
        this.rowId = rowId;
        this.descricao = descricao;
        this.valor = valor;
    }

    /**
     * Construtor para criação (sem rowId).
     * 
     * @param descricao Descrição do gasto
     * @param valor Valor monetário do gasto
     */
    public Expense(String descricao, Double valor) {
        this.descricao = descricao;
        this.valor = valor;
    }
    
    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    /**
     * Obtém a descrição do gasto.
     * 
     * @return Descrição do gasto
     */
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Define a descrição do gasto.
     * 
     * @param descricao Descrição do gasto
     */
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    /**
     * Obtém o valor do gasto.
     * 
     * @return Valor monetário do gasto
     */
    public Double getValor() {
        return valor;
    }
    
    /**
     * Define o valor do gasto.
     * 
     * @param valor Valor monetário do gasto
     */
    public void setValor(Double valor) {
        this.valor = valor;
    }
}
