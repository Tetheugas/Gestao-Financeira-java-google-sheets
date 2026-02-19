package com.financeiro.service;

import com.financeiro.model.Expense;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para GoogleSheetsService.
 */
@SpringBootTest
class GoogleSheetsServiceTest {

    /**
     * Testa o método getColumnLetterForMonth usando reflexão (método privado).
     */
    @Test
    void testGetColumnLetterForMonth_AllMonths() throws Exception {
        GoogleSheetsService service = new GoogleSheetsService();
        Method method = GoogleSheetsService.class.getDeclaredMethod("getColumnLetterForMonth", String.class);
        method.setAccessible(true);

        // Testa todos os meses válidos
        assertEquals("B", method.invoke(service, "Janeiro"));
        assertEquals("C", method.invoke(service, "Fevereiro"));
        assertEquals("D", method.invoke(service, "Março"));
        assertEquals("E", method.invoke(service, "Abril"));
        assertEquals("F", method.invoke(service, "Maio"));
        assertEquals("G", method.invoke(service, "Junho"));
        assertEquals("H", method.invoke(service, "Julho"));
        assertEquals("I", method.invoke(service, "Agosto"));
        assertEquals("J", method.invoke(service, "Setembro"));
        assertEquals("K", method.invoke(service, "Outubro"));
        assertEquals("L", method.invoke(service, "Novembro"));
        assertEquals("M", method.invoke(service, "Dezembro"));
    }

    @Test
    void testGetColumnLetterForMonth_CaseInsensitive() throws Exception {
        GoogleSheetsService service = new GoogleSheetsService();
        Method method = GoogleSheetsService.class.getDeclaredMethod("getColumnLetterForMonth", String.class);
        method.setAccessible(true);

        // Testa normalização de case
        assertEquals("B", method.invoke(service, "janeiro"));
        assertEquals("C", method.invoke(service, "FEVEREIRO"));
        assertEquals("D", method.invoke(service, "mArÇo"));
    }

    @Test
    void testGetColumnLetterForMonth_WithWhitespace() throws Exception {
        GoogleSheetsService service = new GoogleSheetsService();
        Method method = GoogleSheetsService.class.getDeclaredMethod("getColumnLetterForMonth", String.class);
        method.setAccessible(true);

        // Testa com espaços em branco
        assertEquals("B", method.invoke(service, "  Janeiro  "));
        assertEquals("C", method.invoke(service, " Fevereiro "));
    }

    @Test
    void testGetColumnLetterForMonth_InvalidMonth() throws Exception {
        GoogleSheetsService service = new GoogleSheetsService();
        Method method = GoogleSheetsService.class.getDeclaredMethod("getColumnLetterForMonth", String.class);
        method.setAccessible(true);

        // Testa mês inválido
        try {
            method.invoke(service, "InvalidMonth");
            fail("Deveria lançar IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertTrue(e.getCause().getMessage().contains("Mês inválido"));
        }
    }

    @Test
    void testGetColumnLetterForMonth_NullMonth() throws Exception {
        GoogleSheetsService service = new GoogleSheetsService();
        Method method = GoogleSheetsService.class.getDeclaredMethod("getColumnLetterForMonth", String.class);
        method.setAccessible(true);

        // Testa mês nulo
        try {
            method.invoke(service, (String) null);
            fail("Deveria lançar IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertTrue(e.getCause().getMessage().contains("não pode ser nulo"));
        }
    }

    @Test
    void testGetColumnLetterForMonth_EmptyMonth() throws Exception {
        GoogleSheetsService service = new GoogleSheetsService();
        Method method = GoogleSheetsService.class.getDeclaredMethod("getColumnLetterForMonth", String.class);
        method.setAccessible(true);

        // Testa mês vazio
        try {
            method.invoke(service, "");
            fail("Deveria lançar IllegalArgumentException");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertTrue(e.getCause().getMessage().contains("não pode ser nulo ou vazio"));
        }
    }

    /**
     * NOTA: Testes para findNextEmptyRow requerem integração com Google Sheets API real
     * ou refatoração para injeção de dependência do Sheets service.
     * 
     * O método findNextEmptyRow foi implementado com a seguinte lógica:
     * 1. Lê valores da coluna especificada a partir da linha 2 (linha 1 é header)
     * 2. Se não há valores, retorna linha 2
     * 3. Itera pelos valores até encontrar célula vazia (null, empty, ou whitespace)
     * 4. Retorna o número da primeira linha vazia encontrada
     * 5. Se todas as linhas estão preenchidas, retorna a próxima após a última
     * 
     * Casos tratados:
     * - Planilha vazia: retorna 2
     * - Linha vazia no meio: retorna a primeira encontrada
     * - Células com apenas espaços: tratadas como vazias
     * - Todas as linhas preenchidas: retorna próxima após a última
     * 
     * Testes de integração devem ser executados com uma planilha real do Google Sheets.
     */

    /**
     * NOTA: Testes para readExpenses requerem integração com Google Sheets API real
     * ou refatoração para injeção de dependência do Sheets service.
     * 
     * O método readExpenses foi implementado com a seguinte lógica:
     * 1. Converte o mês para letra de coluna usando getColumnLetterForMonth
     * 2. Lê valores do range A2:X1000 (onde X é a coluna do mês)
     * 3. Se não há valores, retorna lista vazia
     * 4. Para cada linha:
     *    - Obtém descrição da coluna A (índice 0)
     *    - Obtém valor da coluna do mês (índice calculado)
     *    - Pula linhas sem descrição ou sem valor
     *    - Remove formatação do valor (R$, pontos, vírgulas)
     *    - Converte para Double e cria objeto Expense
     *    - Trata NumberFormatException e continua processando
     * 5. Retorna lista de Expense com todos os gastos válidos
     * 
     * Casos tratados:
     * - Planilha vazia: retorna lista vazia
     * - Linhas sem descrição: puladas
     * - Linhas sem valor para o mês: puladas
     * - Valores formatados (R$ 1.234,56): formatação removida e convertida
     * - Valores inválidos: logados como warning e linha pulada
     * - Mês inválido: IllegalArgumentException lançada por getColumnLetterForMonth
     * - Erros de comunicação: IOException propagada
     * 
     * Testes de integração devem ser executados com uma planilha real do Google Sheets.
     * 
     * Validação dos requisitos:
     * - Requirement 1.1: Lê dados da coluna correspondente ao mês
     * - Requirement 1.4: Retorna lista vazia se coluna não existir
     * - Requirement 3.3: Usa Google Sheets API v4 para buscar dados
     */

    /**
     * Teste básico para verificar que readExpenses não lança exceção com mês válido.
     * Este teste verifica apenas a validação do mês, não a integração com Google Sheets.
     */
    @Test
    void testReadExpenses_ValidMonth_NoException() {
        GoogleSheetsService service = new GoogleSheetsService();
        
        // Verifica que meses válidos não lançam IllegalArgumentException
        // (A IOException será lançada porque não há credenciais configuradas,
        // mas isso é esperado em testes unitários sem integração)
        assertDoesNotThrow(() -> {
            try {
                service.readExpenses("TestSheet", "Janeiro");
            } catch (java.io.IOException e) {
                // IOException é esperada sem credenciais configuradas
                // O importante é que IllegalArgumentException não seja lançada
            }
        });
    }

    /**
     * Teste para verificar que readExpenses lança exceção com mês inválido.
     */
    @Test
    void testReadExpenses_InvalidMonth_ThrowsException() {
        GoogleSheetsService service = new GoogleSheetsService();
        
        // Verifica que mês inválido lança IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.readExpenses("TestSheet", "InvalidMonth");
            } catch (java.io.IOException e) {
                // Se IOException for lançada primeiro, re-lança para o teste falhar
                throw new RuntimeException("IOException não deveria ser lançada antes da validação do mês", e);
            }
        });
    }

    /**
     * NOTA: Testes para addExpense requerem integração com Google Sheets API real
     * ou refatoração para injeção de dependência do Sheets service.
     * 
     * O método addExpense foi implementado com a seguinte lógica:
     * 1. Valida que expense não é nulo
     * 2. Valida que descrição não é nula ou vazia
     * 3. Valida que valor não é nulo
     * 4. Converte o mês para letra de coluna usando getColumnLetterForMonth
     * 5. Encontra a próxima linha vazia usando findNextEmptyRow na coluna A
     * 6. Atualiza a célula da descrição (coluna A) com o valor da descrição
     * 7. Atualiza a célula do valor (coluna do mês) com o valor numérico
     * 8. Usa setValueInputOption("RAW") para inserir valores sem formatação
     * 
     * Casos tratados:
     * - Expense nulo: IllegalArgumentException
     * - Descrição nula ou vazia: IllegalArgumentException
     * - Valor nulo: IllegalArgumentException
     * - Mês inválido: IllegalArgumentException lançada por getColumnLetterForMonth
     * - Erros de comunicação: IOException propagada
     * 
     * Validação dos requisitos:
     * - Requirement 2.2: Identifica próxima linha vazia usando findNextEmptyRow
     * - Requirement 2.3: Insere descrição e valor nas células apropriadas
     * - Requirement 3.4: Usa Google Sheets API v4 para atualizar células
     * 
     * Testes de integração devem ser executados com uma planilha real do Google Sheets.
     */

    /**
     * Teste para verificar que addExpense valida expense nulo.
     */
    @Test
    void testAddExpense_NullExpense_ThrowsException() {
        GoogleSheetsService service = new GoogleSheetsService();
        
        // Verifica que expense nulo lança IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.addExpense("TestSheet", "Janeiro", null);
            } catch (java.io.IOException e) {
                // Se IOException for lançada primeiro, re-lança para o teste falhar
                throw new RuntimeException("IOException não deveria ser lançada antes da validação", e);
            }
        });
        
        assertTrue(exception.getMessage().contains("não pode ser nulo"));
    }

    /**
     * Teste para verificar que addExpense valida descrição vazia.
     */
    @Test
    void testAddExpense_EmptyDescription_ThrowsException() {
        GoogleSheetsService service = new GoogleSheetsService();
        Expense expense = new Expense("", 100.0);
        
        // Verifica que descrição vazia lança IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.addExpense("TestSheet", "Janeiro", expense);
            } catch (java.io.IOException e) {
                // Se IOException for lançada primeiro, re-lança para o teste falhar
                throw new RuntimeException("IOException não deveria ser lançada antes da validação", e);
            }
        });
        
        assertTrue(exception.getMessage().contains("Descrição"));
    }

    /**
     * Teste para verificar que addExpense valida descrição nula.
     */
    @Test
    void testAddExpense_NullDescription_ThrowsException() {
        GoogleSheetsService service = new GoogleSheetsService();
        Expense expense = new Expense(null, 100.0);
        
        // Verifica que descrição nula lança IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.addExpense("TestSheet", "Janeiro", expense);
            } catch (java.io.IOException e) {
                // Se IOException for lançada primeiro, re-lança para o teste falhar
                throw new RuntimeException("IOException não deveria ser lançada antes da validação", e);
            }
        });
        
        assertTrue(exception.getMessage().contains("Descrição"));
    }

    /**
     * Teste para verificar que addExpense valida valor nulo.
     */
    @Test
    void testAddExpense_NullValue_ThrowsException() {
        GoogleSheetsService service = new GoogleSheetsService();
        Expense expense = new Expense("Test", null);
        
        // Verifica que valor nulo lança IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.addExpense("TestSheet", "Janeiro", expense);
            } catch (java.io.IOException e) {
                // Se IOException for lançada primeiro, re-lança para o teste falhar
                throw new RuntimeException("IOException não deveria ser lançada antes da validação", e);
            }
        });
        
        assertTrue(exception.getMessage().contains("Valor"));
    }

    /**
     * Teste para verificar que addExpense valida mês inválido.
     */
    @Test
    void testAddExpense_InvalidMonth_ThrowsException() {
        GoogleSheetsService service = new GoogleSheetsService();
        Expense expense = new Expense("Test", 100.0);
        
        // Verifica que mês inválido lança IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.addExpense("TestSheet", "InvalidMonth", expense);
            } catch (java.io.IOException e) {
                // Se IOException for lançada primeiro, re-lança para o teste falhar
                throw new RuntimeException("IOException não deveria ser lançada antes da validação do mês", e);
            }
        });
    }

    /**
     * Teste básico para verificar que addExpense não lança exceção com dados válidos.
     * Este teste verifica apenas a validação dos parâmetros, não a integração com Google Sheets.
     */
    @Test
    void testAddExpense_ValidData_NoValidationException() {
        GoogleSheetsService service = new GoogleSheetsService();
        Expense expense = new Expense("Test Expense", 100.0);
        
        // Verifica que dados válidos não lançam IllegalArgumentException
        // (A IOException será lançada porque não há credenciais configuradas,
        // mas isso é esperado em testes unitários sem integração)
        assertDoesNotThrow(() -> {
            try {
                service.addExpense("TestSheet", "Janeiro", expense);
            } catch (java.io.IOException e) {
                // IOException é esperada sem credenciais configuradas
                // O importante é que IllegalArgumentException não seja lançada
            }
        });
    }
}
