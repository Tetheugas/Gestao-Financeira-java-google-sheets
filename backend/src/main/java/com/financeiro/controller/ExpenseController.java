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
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ExpenseController {

    @Autowired
    private GoogleSheetsService sheetsService;

    /**
     * Endpoint GET para listar os filtros (abas) disponíveis.
     *
     * @return Lista de nomes das abas
     * @throws IOException se houver erro ao acessar Google Sheets
     */
    @GetMapping("/filters")
    public ResponseEntity<List<String>> getFilters() throws IOException {
        List<String> filters = sheetsService.getSheetNames();
        return ResponseEntity.ok(filters);
    }

    /**
     * Endpoint POST para criar um novo filtro (aba).
     *
     * @param body Mapa contendo o nome do filtro (chave "name")
     * @return MessageDTO com status
     * @throws IOException se houver erro ao acessar Google Sheets
     */
    @PostMapping("/filters")
    public ResponseEntity<MessageDTO> createFilter(@RequestBody Map<String, String> body) throws IOException {
        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageDTO("Nome do filtro é obrigatório", "error"));
        }

        try {
            sheetsService.createSheet(name);
            return ResponseEntity.status(201)
                    .body(new MessageDTO("Filtro criado com sucesso", "success"));
        } catch (IllegalArgumentException e) {
             return ResponseEntity.badRequest()
                    .body(new MessageDTO(e.getMessage(), "error"));
        }
    }

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
    @GetMapping("/expenses/{mes}")
    public ResponseEntity<List<ExpenseDTO>> getExpenses(
            @PathVariable String mes,
            @RequestParam String aba) throws IOException {

        // Chamar serviço para ler gastos
        List<Expense> expenses = sheetsService.readExpenses(aba, mes);

        // Converter List<Expense> para List<ExpenseDTO> formatando valores
        List<ExpenseDTO> expenseDTOs = expenses.stream()
                .map(expense -> new ExpenseDTO(
                        expense.getRowId(),
                        expense.getDescricao(),
                        MoneyFormatter.formatToBRL(expense.getValor()),
                        expense.getValor()
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
    @PostMapping("/expenses")
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

    /**
     * Endpoint PUT para atualizar um gasto existente.
     *
     * @param rowId ID da linha a ser atualizada
     * @param request ExpenseRequestDTO com os novos dados
     * @return MessageDTO com status
     * @throws IOException se houver erro ao acessar Google Sheets
     */
    @PutMapping("/expenses/{rowId}")
    public ResponseEntity<MessageDTO> updateExpense(
            @PathVariable int rowId,
            @RequestBody @jakarta.validation.Valid ExpenseRequestDTO request) throws IOException {

        // Validação adicional
        if (request.getValor() == null || request.getValor().isNaN() || request.getValor().isInfinite()) {
             return ResponseEntity.badRequest()
                    .body(new MessageDTO("Valor deve ser um número válido", "error"));
        }

        Expense expense = new Expense(request.getDescricao(), request.getValor());

        try {
            sheetsService.updateExpense(request.getAba(), request.getMes(), rowId, expense);
            return ResponseEntity.ok(new MessageDTO("Gasto atualizado com sucesso", "success"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageDTO(e.getMessage(), "error"));
        }
    }

    /**
     * Endpoint DELETE para remover um gasto.
     *
     * @param rowId ID da linha a ser removida
     * @param aba Nome da aba/sheet onde o gasto está
     * @return MessageDTO com status
     * @throws IOException se houver erro ao acessar Google Sheets
     */
    @DeleteMapping("/expenses/{rowId}")
    public ResponseEntity<MessageDTO> deleteExpense(
            @PathVariable int rowId,
            @RequestParam String aba) throws IOException {

        try {
            sheetsService.deleteExpense(aba, rowId);
            return ResponseEntity.ok(new MessageDTO("Gasto removido com sucesso", "success"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageDTO(e.getMessage(), "error"));
        }
    }

    @ExceptionHandler(GoogleSheetsAuthException.class)
    public ResponseEntity<MessageDTO> handleAuthException(GoogleSheetsAuthException e) {
        return ResponseEntity.status(401)
                .body(new MessageDTO(e.getMessage(), "auth_error"));
    }

}
