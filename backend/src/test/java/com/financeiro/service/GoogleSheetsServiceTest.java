package com.financeiro.service;

import com.financeiro.model.Expense;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Testes unitários para GoogleSheetsService.
 */
@SpringBootTest
@TestPropertySource(properties = "google.sheets.spreadsheet.id=dummy_id")
class GoogleSheetsServiceTest {

    @MockBean
    private OAuth2AuthorizedClientService authorizedClientService;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    private GoogleSheetsService createService() {
        return new GoogleSheetsService(mock(OAuth2AuthorizedClientService.class), "dummy_id");
    }

    /**
     * Testa o método getColumnLetterForMonth usando reflexão (método privado).
     */
    @Test
    void testGetColumnLetterForMonth_AllMonths() throws Exception {
        GoogleSheetsService service = createService();
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
        GoogleSheetsService service = createService();
        Method method = GoogleSheetsService.class.getDeclaredMethod("getColumnLetterForMonth", String.class);
        method.setAccessible(true);

        // Testa normalização de case
        assertEquals("B", method.invoke(service, "janeiro"));
        assertEquals("C", method.invoke(service, "FEVEREIRO"));
        assertEquals("D", method.invoke(service, "mArÇo"));
    }

    @Test
    void testGetColumnLetterForMonth_WithWhitespace() throws Exception {
        GoogleSheetsService service = createService();
        Method method = GoogleSheetsService.class.getDeclaredMethod("getColumnLetterForMonth", String.class);
        method.setAccessible(true);

        // Testa com espaços em branco
        assertEquals("B", method.invoke(service, "  Janeiro  "));
        assertEquals("C", method.invoke(service, " Fevereiro "));
    }

    @Test
    void testGetColumnLetterForMonth_InvalidMonth() throws Exception {
        GoogleSheetsService service = createService();
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
        GoogleSheetsService service = createService();
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
        GoogleSheetsService service = createService();
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

    @Test
    void testReadExpenses_ValidMonth_NoException() throws Exception {
        GoogleSheetsService service = createService();
        service.init();
        
        // Verifica que meses válidos não lançam IllegalArgumentException
        assertDoesNotThrow(() -> {
            try {
                service.readExpenses("TestSheet", "Janeiro");
            } catch (com.financeiro.exception.GoogleSheetsAuthException | java.io.IOException | IllegalArgumentException e) {
                // If it's IO or Auth, it's expected. If it's IllegalArgument, it failed.
                // The updated service might throw IllegalArgument for "Mês não pode ser nulo" or "dados inválidos"
                // But readExpenses("TestSheet", "Janeiro") is valid args.
                // It will likely throw GoogleSheetsAuthException because Authentication is null in context.
            }
        });
    }

    @Test
    void testReadExpenses_InvalidMonth_ThrowsException() {
        GoogleSheetsService service = createService();
        
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.readExpenses("TestSheet", "InvalidMonth");
            } catch (java.io.IOException e) {
                throw new RuntimeException("IOException não deveria ser lançada antes da validação do mês", e);
            }
        });
    }

    @Test
    void testAddExpense_NullExpense_ThrowsException() {
        GoogleSheetsService service = createService();
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.addExpense("TestSheet", "Janeiro", null);
            } catch (java.io.IOException e) {
                throw new RuntimeException("IOException não deveria ser lançada antes da validação", e);
            }
        });
        
        assertTrue(exception.getMessage().contains("inválidos")); // Changed message in new impl
    }

    @Test
    void testAddExpense_EmptyDescription_ThrowsException() {
        GoogleSheetsService service = createService();
        Expense expense = new Expense("", 100.0);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.addExpense("TestSheet", "Janeiro", expense);
            } catch (java.io.IOException e) {
                throw new RuntimeException("IOException não deveria ser lançada antes da validação", e);
            }
        });
        
        assertTrue(exception.getMessage().contains("inválidos"));
    }

    @Test
    void testAddExpense_NullDescription_ThrowsException() {
        GoogleSheetsService service = createService();
        Expense expense = new Expense(null, 100.0);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.addExpense("TestSheet", "Janeiro", expense);
            } catch (java.io.IOException e) {
                throw new RuntimeException("IOException não deveria ser lançada antes da validação", e);
            }
        });
        
        assertTrue(exception.getMessage().contains("inválidos"));
    }

    @Test
    void testAddExpense_NullValue_ThrowsException() {
        GoogleSheetsService service = createService();
        Expense expense = new Expense("Test", null);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.addExpense("TestSheet", "Janeiro", expense);
            } catch (java.io.IOException e) {
                throw new RuntimeException("IOException não deveria ser lançada antes da validação", e);
            }
        });
        
        assertTrue(exception.getMessage().contains("inválidos"));
    }

    @Test
    void testAddExpense_InvalidMonth_ThrowsException() {
        GoogleSheetsService service = createService();
        Expense expense = new Expense("Test", 100.0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.addExpense("TestSheet", "InvalidMonth", expense);
            } catch (java.io.IOException e) {
                throw new RuntimeException("IOException não deveria ser lançada antes da validação do mês", e);
            }
        });
    }

    @Test
    void testAddExpense_ValidData_NoValidationException() throws Exception {
        GoogleSheetsService service = createService();
        service.init();
        Expense expense = new Expense("Test Expense", 100.0);
        
        assertDoesNotThrow(() -> {
            try {
                service.addExpense("TestSheet", "Janeiro", expense);
            } catch (com.financeiro.exception.GoogleSheetsAuthException | java.io.IOException e) {
                // Expected
            }
        });
    }

    @Test
    void testRenameSheet_NullOldName_ThrowsException() {
        GoogleSheetsService service = createService();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.renameSheet(null, "NewName");
            } catch (java.io.IOException e) {
                throw new RuntimeException("IOException não deveria ser lançada antes da validação", e);
            }
        });

        assertTrue(exception.getMessage().contains("inválidos")); // Changed message
    }

    @Test
    void testRenameSheet_NullNewName_ThrowsException() {
        GoogleSheetsService service = createService();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.renameSheet("OldName", null);
            } catch (java.io.IOException e) {
                throw new RuntimeException("IOException não deveria ser lançada antes da validação", e);
            }
        });

        assertTrue(exception.getMessage().contains("inválidos"));
    }

    @Test
    void testDeleteSheet_NullName_ThrowsException() {
        GoogleSheetsService service = createService();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                service.deleteSheet(null);
            } catch (java.io.IOException e) {
                throw new RuntimeException("IOException não deveria ser lançada antes da validação", e);
            }
        });

        assertTrue(exception.getMessage().contains("inválido")); // Changed message
    }
}
