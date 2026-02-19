package com.financeiro.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Utilitário para formatação de valores monetários no padrão brasileiro (BRL).
 * 
 * Fornece métodos estáticos para converter entre Double e String formatada
 * seguindo o padrão: R$ X.XXX,XX (símbolo R$, ponto como separador de milhares,
 * vírgula como separador decimal, duas casas decimais).
 */
public class MoneyFormatter {
    
    private static final Locale PT_BR = new Locale("pt", "BR");
    
    /**
     * Formata um valor Double para String no padrão monetário brasileiro.
     * 
     * @param value Valor a ser formatado
     * @return String formatada no padrão BRL (ex: "R$ 1.234,56")
     * @throws IllegalArgumentException se value for null
     */
    public static String formatToBRL(Double value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(PT_BR);
        return formatter.format(value);
    }
    
    /**
     * Converte uma String no formato BRL para Double.
     * 
     * @param value String formatada em BRL (ex: "R$ 1.234,56")
     * @return Valor numérico como Double
     * @throws ParseException se a string não puder ser parseada
     * @throws IllegalArgumentException se value for null ou vazia
     */
    public static Double parseBRL(String value) throws ParseException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Value cannot be null or empty");
        }
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(PT_BR);
        return formatter.parse(value).doubleValue();
    }
}
