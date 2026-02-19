package com.financeiro.controller;

import com.financeiro.dto.ExpenseDTO;
import com.financeiro.dto.ExpenseRequestDTO;
import com.financeiro.dto.MessageDTO;
import com.financeiro.exception.GoogleSheetsAuthException;
import com.financeiro.model.Expense;
import com.financeiro.service.GoogleSheetsService;
import com.financeiro.util.MoneyFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "http://localhost:5173")
public class ExpenseController {

    @Autowired
    private GoogleSheetsService sheetsService;

    /**
     * Endpoint GET para leitura de gastos de um mês específico.
     *
     * @param mes Mês para buscar gastos (ex: "Fevereiro")
     * @param aba Nome da aba/sheet na planilha (ex: "CartãoNubank")
     * @return Lista de ExpenseDTO com gastos formatados
     * @throws IOException se houver erro ao acessar Google Sheets
     *
     * Requirements: 1.1, 1.2, 1.3, 4.1
     */
    @GetMapping("/{mes}")
    public ResponseEntity<List<ExpenseDTO>> getExpenses(
            @PathVariable String mes,
            @RequestParam String aba) throws IOException {

        // Chamar serviço para ler gastos
        List<Expense> expenses = sheetsService.readExpenses(aba, mes);

        // Converter List<Expense> para List<ExpenseDTO> formatando valores
        List<ExpenseDTO> expenseDTOs = expenses.stream()
                .map(expense -> new ExpenseDTO(
                        expense.getDescricao(),
                        MoneyFormatter.formatToBRL(expense.getValor())
                ))
                .collect(Collectors.toList());

        // Retornar ResponseEntity com lista de ExpenseDTO
        return ResponseEntity.ok(expenseDTOs);
    }


    /**
     * Endpoint POST para cadastro de novos gastos.
     *
     * @param request ExpenseRequestDTO com dados do gasto (descrição, valor, aba, mês)
     * @return ResponseEntity com HTTP 201 e MessageDTO de sucesso
     * @throws IOException se houver erro ao acessar Google Sheets
     *
     * Requirements: 2.1, 2.2, 2.3, 2.4, 4.2, 4.3
     */
    @PostMapping
    public ResponseEntity<MessageDTO> addExpense(
            @RequestBody @jakarta.validation.Valid ExpenseRequestDTO request) throws IOException {

        // Validação adicional: verificar se valor é numérico válido (não NaN ou infinito)
        if (request.getValor() == null || request.getValor().isNaN() || request.getValor().isInfinite()) {
            return ResponseEntity.badRequest()
                    .body(new MessageDTO("Valor deve ser um número válido", "error"));
        }

        // Criar objeto Expense a partir do DTO
        Expense expense = new Expense(request.getDescricao(), request.getValor());

        // Chamar serviço para adicionar gasto na planilha
        sheetsService.addExpense(request.getAba(), request.getMes(), expense);

        // Retornar ResponseEntity com HTTP 201 e MessageDTO de sucesso
        return ResponseEntity.status(201)
                .body(new MessageDTO("Gasto cadastrado com sucesso", "success"));
    }

    @ExceptionHandler(GoogleSheetsAuthException.class)
    public ResponseEntity<MessageDTO> handleAuthException(GoogleSheetsAuthException e) {
        return ResponseEntity.status(401)
                .body(new MessageDTO(e.getMessage(), "auth_error"));
    }

}

