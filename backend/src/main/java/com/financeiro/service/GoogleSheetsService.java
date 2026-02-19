package com.financeiro.service;

import com.financeiro.exception.GoogleSheetsAuthException;
import com.financeiro.model.Expense;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Serviço responsável pela integração com Google Sheets API v4.
 * Gerencia autenticação OAuth2 e operações de leitura/escrita na planilha.
 */
@Service
public class GoogleSheetsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleSheetsService.class);
    private static final String APPLICATION_NAME = "Gestao Financeira Pessoal";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    private GoogleAuthorizationCodeFlow flow;
    private NetHttpTransport httpTransport;

    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            InputStream in = GoogleSheetsService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                logger.error("Arquivo credentials.json não encontrado no classpath");
                throw new FileNotFoundException("Arquivo credentials.json não encontrado. " +
                        "Por favor, coloque o arquivo credentials.json em src/main/resources/");
            }

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            // Configura o fluxo de autorização e armazena credenciais
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
        } catch (Exception e) {
            logger.error("Failed to initialize GoogleSheetsService", e);
            throw e;
        }
    }

    public boolean isAuthorized() throws IOException {
        if (flow == null) return false;
        Credential credential = flow.loadCredential("user");
        return credential != null && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() == null || credential.getExpiresInSeconds() > 60);
    }

    public String getAuthorizationUrl() throws IOException, ExecutionException, InterruptedException {
        CompletableFuture<String> urlFuture = new CompletableFuture<>();

        // We use a custom browser to capture the URL instead of opening it
        AuthorizationCodeInstalledApp.Browser browser = new AuthorizationCodeInstalledApp.Browser() {
            @Override
            public void browse(String url) throws IOException {
                urlFuture.complete(url);
            }
        };

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        AuthorizationCodeInstalledApp app = new AuthorizationCodeInstalledApp(flow, receiver, browser);

        // Run authorize in a separate thread because it blocks waiting for the code
        new Thread(() -> {
            try {
                app.authorize("user");
            } catch (IOException e) {
                logger.error("Error during authorization flow", e);
                urlFuture.completeExceptionally(e);
            }
        }).start();

        // Wait for the URL to be generated
        return urlFuture.get();
    }

    /**
     * Cria uma credencial OAuth2 autorizada.
     *
     * @return Uma credencial OAuth2 autorizada.
     * @throws IOException Se o arquivo credentials.json não for encontrado.
     */
    private Credential getCredentials() throws IOException {
        Credential credential = flow.loadCredential("user");
        if (credential == null) {
            throw new GoogleSheetsAuthException("User not authenticated. Please login via /api/auth/login");
        }

        // Ensure token is valid/refreshed
        if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() < 60) {
            if (!credential.refreshToken()) {
                throw new GoogleSheetsAuthException("Failed to refresh token. Please login again.");
            }
        }

        return credential;
    }

    /**
     * Obtém uma instância autenticada do serviço Google Sheets.
     *
     * @return Instância do serviço Sheets autenticada.
     * @throws GoogleSheetsAuthException Se houver erro na autenticação.
     */
    private Sheets getSheetsService() throws GoogleSheetsAuthException {
        try {
            Credential credential = getCredentials();
            
            logger.info("Autenticação com Google Sheets realizada com sucesso");
            
            return new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
                    
        } catch (IOException e) {
            logger.error("Erro de I/O durante autenticação com Google Sheets", e);
            throw new GoogleSheetsAuthException(
                    "Erro ao ler arquivo de credenciais ou tokens de autenticação", 
                    e);
        }
    }


    /**
     * Converte o nome do mês em português para a letra da coluna correspondente na planilha.
     * A coluna A é reservada para descrições, então Janeiro começa na coluna B.
     *
     * @param month Nome do mês em português (ex: "Janeiro", "Fevereiro")
     * @return Letra da coluna correspondente (ex: "B" para Janeiro, "C" para Fevereiro)
     * @throws IllegalArgumentException Se o mês fornecido for inválido
     */
    private String getColumnLetterForMonth(String month) {
        if (month == null || month.trim().isEmpty()) {
            throw new IllegalArgumentException("Mês não pode ser nulo ou vazio");
        }

        // Normaliza o mês para primeira letra maiúscula e resto minúsculo
        String normalizedMonth = month.trim().substring(0, 1).toUpperCase() +
                                 month.trim().substring(1).toLowerCase();

        switch (normalizedMonth) {
            case "Janeiro":
                return "B";
            case "Fevereiro":
                return "C";
            case "Março":
                return "D";
            case "Marco":
                return "D";
            case "Abril":
                return "E";
            case "Maio":
                return "F";
            case "Junho":
                return "G";
            case "Julho":
                return "H";
            case "Agosto":
                return "I";
            case "Setembro":
                return "J";
            case "Outubro":
                return "K";
            case "Novembro":
                return "L";
            case "Dezembro":
                return "M";
            default:
                logger.warn("Mês inválido fornecido: {}", month);
                throw new IllegalArgumentException("Mês inválido: " + month +
                        ". Use um nome de mês válido em português (ex: Janeiro, Fevereiro, etc.)");
        }
    }


    /**
     * Identifica a próxima linha vazia em uma coluna específica da planilha.
     * Começa a busca a partir da linha 2 (linha 1 é o header).
     *
     * @param sheetName Nome da aba da planilha (ex: "CartãoNubank")
     * @param columnLetter Letra da coluna a ser verificada (ex: "A", "B", "C")
     * @return Número da primeira linha vazia encontrada
     * @throws IOException Se houver erro ao comunicar com Google Sheets API
     * @throws GoogleSheetsAuthException Se houver erro de autenticação
     */
    private int findNextEmptyRow(String sheetName, String columnLetter) throws IOException {
        Sheets sheetsService = getSheetsService();

        // Define o range para ler a coluna inteira a partir da linha 2
        // Lê até a linha 1000 para ter um limite razoável
        String range = String.format("%s!%s2:%s1000", sheetName, columnLetter, columnLetter);

        logger.debug("Buscando próxima linha vazia em: {}", range);

        try {

            var result = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = result.getValues();

            // Se não há valores, a primeira linha vazia é a linha 2
            if (values == null || values.isEmpty()) {
                logger.debug("Nenhum valor encontrado, próxima linha vazia é 2");
                return 2;
            }

            // Itera pelos valores para encontrar a primeira célula vazia
            for (int i = 0; i < values.size(); i++) {
                List<Object> row = values.get(i);

                // Se a linha está vazia ou a célula está vazia
                if (row == null || row.isEmpty() || row.get(0) == null ||
                    row.get(0).toString().trim().isEmpty()) {
                    int emptyRow = i + 2; // +2 porque começamos da linha 2
                    logger.debug("Primeira linha vazia encontrada: {}", emptyRow);
                    return emptyRow;
                }
            }

            // Se todas as linhas estão preenchidas, retorna a próxima após a última
            int nextRow = values.size() + 2;
            logger.debug("Todas as linhas estão preenchidas, próxima linha vazia: {}", nextRow);
            return nextRow;

        } catch (IOException e) {
            logger.error("Erro ao buscar próxima linha vazia em {}!{}", sheetName, columnLetter, e);
            throw e;
        }
    }


    /**
     * Verifica se uma aba existe na planilha.
     *
     * @param sheetName Nome da aba a ser verificada (ex: "CartãoNubank")
     * @return true se a aba existe, false caso contrário
     * @throws GoogleSheetsAuthException Se houver erro de autenticação
     */
    private boolean sheetExists(String sheetName) throws GoogleSheetsAuthException {
        try {
            Sheets sheetsService = getSheetsService();

            Spreadsheet spreadsheet = sheetsService.spreadsheets()
                    .get(spreadsheetId)
                    .setFields("sheets.properties.title")
                    .execute();

            if (spreadsheet.getSheets() == null) {
                return false;
            }

            for (Sheet sheet : spreadsheet.getSheets()) {
                if (sheet.getProperties().getTitle().equals(sheetName)) {
                    logger.debug("Aba '{}' encontrada na planilha", sheetName);
                    return true;
                }
            }

            logger.debug("Aba '{}' não encontrada na planilha", sheetName);
            return false;

        } catch (IOException e) {
            logger.error("Erro ao verificar se a aba '{}' existe", sheetName, e);
            throw new GoogleSheetsAuthException("Erro ao verificar existência da aba", e);
        }
    }


    /**
     * Cria uma nova aba na planilha com headers padrão (meses).
     *
     * @param sheetName Nome da aba a ser criada (ex: "CartãoNubank")
     * @throws GoogleSheetsAuthException Se houver erro de autenticação
     * @throws IOException Se houver erro ao comunicar com Google Sheets API
     */
    public void createSheet(String sheetName) throws GoogleSheetsAuthException, IOException {
        try {
            Sheets sheetsService = getSheetsService();

            // Cria a requisição para adicionar uma nova aba
            BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();

            AddSheetRequest addSheetRequest = new AddSheetRequest();
            SheetProperties sheetProperties = new SheetProperties();
            sheetProperties.setTitle(sheetName);
            addSheetRequest.setProperties(sheetProperties);

            Request request = new Request();
            request.setAddSheet(addSheetRequest);

            batchUpdateRequest.setRequests(Collections.singletonList(request));

            BatchUpdateSpreadsheetResponse response = sheetsService.spreadsheets()
                    .batchUpdate(spreadsheetId, batchUpdateRequest)
                    .execute();

            logger.info("Nova aba '{}' criada com sucesso na planilha", sheetName);

            // Adiciona os headers de mês na primeira linha
            addMonthHeaders(sheetName);

        } catch (IOException e) {
            logger.error("Erro ao criar a aba '{}' na planilha", sheetName, e);
            throw e;
        }
    }


    /**
     * Adiciona os headers dos meses (Janeiro, Fevereiro, etc.) na primeira linha da aba.
     *
     * @param sheetName Nome da aba para adicionar os headers
     * @throws IOException Se houver erro ao comunicar com Google Sheets API
     * @throws GoogleSheetsAuthException Se houver erro de autenticação
     */
    private void addMonthHeaders(String sheetName) throws IOException, GoogleSheetsAuthException {
        Sheets sheetsService = getSheetsService();

        // Cria a lista de headers: coluna A é "Descrição", depois vêm os meses
        List<Object> headers = new ArrayList<>();
        headers.add("Descrição");
        headers.add("Janeiro");
        headers.add("Fevereiro");
        headers.add("Março");
        headers.add("Abril");
        headers.add("Maio");
        headers.add("Junho");
        headers.add("Julho");
        headers.add("Agosto");
        headers.add("Setembro");
        headers.add("Outubro");
        headers.add("Novembro");
        headers.add("Dezembro");

        // Define o range para a primeira linha
        String headerRange = String.format("%s!A1:M1", sheetName);

        ValueRange headerValueRange = new ValueRange()
                .setValues(Collections.singletonList(headers));

        sheetsService.spreadsheets().values()
                .update(spreadsheetId, headerRange, headerValueRange)
                .setValueInputOption("RAW")
                .execute();

        logger.debug("Headers de mês adicionados à aba '{}'", sheetName);
    }


    /**
     * Verifica se a aba existe e, se não existir, cria uma nova com headers padrão.
     *
     * @param sheetName Nome da aba a ser verificada ou criada
     * @throws GoogleSheetsAuthException Se houver erro de autenticação
     * @throws IOException Se houver erro ao comunicar com Google Sheets API
     */
    public void ensureSheetExists(String sheetName) throws GoogleSheetsAuthException, IOException {
        if (!sheetExists(sheetName)) {
            logger.warn("Aba '{}' não existe. Criando nova aba...", sheetName);
            createSheet(sheetName);
        }
    }

    /**
     * Obtém uma lista com os nomes de todas as abas (filtros) disponíveis na planilha.
     *
     * @return Lista de nomes das abas
     * @throws IOException Se houver erro ao comunicar com Google Sheets API
     */
    public List<String> getSheetNames() throws IOException {
        try {
            Sheets sheetsService = getSheetsService();

            Spreadsheet spreadsheet = sheetsService.spreadsheets()
                    .get(spreadsheetId)
                    .setFields("sheets.properties.title")
                    .execute();

            List<String> sheetNames = new ArrayList<>();
            if (spreadsheet.getSheets() != null) {
                for (Sheet sheet : spreadsheet.getSheets()) {
                    sheetNames.add(sheet.getProperties().getTitle());
                }
            }
            return sheetNames;

        } catch (IOException e) {
            logger.error("Erro ao buscar nomes das abas", e);
            throw e;
        }
    }

    /**
     * Obtém o ID da aba (sheetId) a partir do seu nome.
     * Necessário para operações de deleção que requerem sheetId em vez de nome.
     *
     * @param sheetName Nome da aba
     * @return ID da aba (Integer) ou null se não encontrada
     * @throws IOException Se houver erro ao comunicar com Google Sheets API
     */
    private Integer getSheetId(String sheetName) throws IOException {
        try {
            Sheets sheetsService = getSheetsService();

            Spreadsheet spreadsheet = sheetsService.spreadsheets()
                    .get(spreadsheetId)
                    .setFields("sheets.properties.title,sheets.properties.sheetId")
                    .execute();

            if (spreadsheet.getSheets() != null) {
                for (Sheet sheet : spreadsheet.getSheets()) {
                    if (sheet.getProperties().getTitle().equals(sheetName)) {
                        return sheet.getProperties().getSheetId();
                    }
                }
            }
            return null;

        } catch (IOException e) {
            logger.error("Erro ao buscar ID da aba {}", sheetName, e);
            throw e;
        }
    }


    /**
     * Lê os gastos de um mês específico de uma aba da planilha.
     * Retorna uma lista de Expense contendo descrição (coluna A), valor (coluna do mês) e ID da linha.
     *
     * @param sheetName Nome da aba da planilha (ex: "CartãoNubank")
     * @param month Nome do mês em português (ex: "Janeiro", "Fevereiro")
     * @return Lista de gastos do mês especificado, ou lista vazia se não houver dados
     * @throws IOException Se houver erro ao comunicar com Google Sheets API
     * @throws GoogleSheetsAuthException Se houver erro de autenticação
     * @throws IllegalArgumentException Se o mês fornecido for inválido
     */
    public List<Expense> readExpenses(String sheetName, String month) throws IOException {
        // Converte o mês para letra de coluna
        String columnLetter = getColumnLetterForMonth(month);

        // Garante que a aba existe, se não, cria uma nova
        ensureSheetExists(sheetName);

        Sheets sheetsService = getSheetsService();

        // Define o range para ler a coluna A (descrições) e a coluna do mês
        // Lê a partir da linha 2 (linha 1 é o header) até a linha 1000
        String range = String.format("%s!A2:%s1000", sheetName, columnLetter);

        logger.debug("Lendo gastos do mês {} da aba {}: range={}", month, sheetName, range);

        try {
            var result = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = result.getValues();

            // Se não há valores, retorna lista vazia
            if (values == null || values.isEmpty()) {
                logger.info("Nenhum gasto encontrado para {} em {}", month, sheetName);
                return Collections.emptyList();
            }

            // Cria lista de gastos
            List<Expense> expenses = new ArrayList<>();

            // Calcula o índice da coluna do mês (B=1, C=2, etc.)
            int monthColumnIndex = columnLetter.charAt(0) - 'A';

            for (int i = 0; i < values.size(); i++) {
                List<Object> row = values.get(i);

                // Verifica se a linha tem dados suficientes
                if (row == null || row.isEmpty()) {
                    continue;
                }

                // Obtém a descrição (coluna A, índice 0)
                String descricao = row.size() > 0 && row.get(0) != null
                    ? row.get(0).toString().trim()
                    : "";

                // Se não há descrição, pula esta linha
                if (descricao.isEmpty()) {
                    continue;
                }

                // Obtém o valor da coluna do mês
                // O índice na row é relativo ao range (A2:X1000)
                // Coluna A é índice 0, coluna B é índice 1, etc.
                String valorStr = row.size() > monthColumnIndex && row.get(monthColumnIndex) != null
                    ? row.get(monthColumnIndex).toString().trim()
                    : "";

                // Se não há valor para este mês, pula esta linha
                if (valorStr.isEmpty()) {
                    continue;
                }

                // Tenta converter o valor para Double
                try {
                    // Remove formatação se houver (R$, pontos, vírgulas)
                    String cleanValue = valorStr
                        .replace("R$", "")
                        .replace(".", "")
                        .replace(",", ".")
                        .trim();

                    Double valor = Double.parseDouble(cleanValue);

                    // O índice da linha na planilha é i + 2 (porque values começa em Row 2)
                    int rowId = i + 2;

                    // Cria o objeto Expense e adiciona à lista
                    Expense expense = new Expense(rowId, descricao, valor);
                    expenses.add(expense);

                } catch (NumberFormatException e) {
                    logger.warn("Valor inválido encontrado na linha com descrição '{}': {}",
                               descricao, valorStr);
                    // Continua processando as outras linhas
                }
            }

            logger.info("Lidos {} gastos do mês {} da aba {}", expenses.size(), month, sheetName);
            return expenses;

        } catch (IOException e) {
            logger.error("Erro ao ler gastos do mês {} da aba {}", month, sheetName, e);
            throw e;
        }
    }


    /**
     * Adiciona um novo gasto na próxima linha vazia da planilha.
     * Insere a descrição na coluna A e o valor na coluna do mês especificado.
     *
     * @param sheetName Nome da aba da planilha (ex: "CartãoNubank")
     * @param month Nome do mês em português (ex: "Janeiro", "Fevereiro")
     * @param expense Objeto Expense contendo descrição e valor
     * @throws IOException Se houver erro ao comunicar com Google Sheets API
     * @throws GoogleSheetsAuthException Se houver erro de autenticação
     * @throws IllegalArgumentException Se o mês fornecido for inválido ou expense for nulo
     */
    public void addExpense(String sheetName, String month, Expense expense) throws IOException {
        if (expense == null) {
            throw new IllegalArgumentException("Expense não pode ser nulo");
        }

        if (expense.getDescricao() == null || expense.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição do gasto não pode ser vazia");
        }

        if (expense.getValor() == null) {
            throw new IllegalArgumentException("Valor do gasto não pode ser nulo");
        }

        // Converte o mês para letra de coluna
        String columnLetter = getColumnLetterForMonth(month);

        // Garante que a aba existe, se não, cria uma nova
        ensureSheetExists(sheetName);

        // Encontra a próxima linha vazia na coluna A (descrições)
        int nextRow = findNextEmptyRow(sheetName, "A");

        Sheets sheetsService = getSheetsService();

        logger.debug("Adicionando gasto '{}' com valor {} na linha {} do mês {} (coluna {})",
                    expense.getDescricao(), expense.getValor(), nextRow, month, columnLetter);

        try {
            // Prepara os dados para inserção
            // Precisamos atualizar duas células: descrição (coluna A) e valor (coluna do mês)

            // Atualiza a descrição na coluna A
            String descricaoRange = String.format("%s!A%d", sheetName, nextRow);
            com.google.api.services.sheets.v4.model.ValueRange descricaoValueRange =
                new com.google.api.services.sheets.v4.model.ValueRange()
                    .setValues(Collections.singletonList(
                        Collections.singletonList(expense.getDescricao())
                    ));

            sheetsService.spreadsheets().get(spreadsheetId);

            sheetsService.spreadsheets().values()
                .update(spreadsheetId, descricaoRange, descricaoValueRange)
                .setValueInputOption("RAW")
                .execute();

            logger.debug("Descrição inserida em {}", descricaoRange);

            // Atualiza o valor na coluna do mês
            String valorRange = String.format("%s!%s%d", sheetName, columnLetter, nextRow);
            com.google.api.services.sheets.v4.model.ValueRange valorValueRange =
                new com.google.api.services.sheets.v4.model.ValueRange()
                    .setValues(Collections.singletonList(
                        Collections.singletonList(expense.getValor())
                    ));

            sheetsService.spreadsheets().values()
                .update(spreadsheetId, valorRange, valorValueRange)
                .setValueInputOption("RAW")
                .execute();

            logger.debug("Valor inserido em {}", valorRange);

            logger.info("Gasto '{}' adicionado com sucesso na aba {} para o mês {}",
                       expense.getDescricao(), sheetName, month);

        } catch (IOException e) {
            logger.error("Erro ao adicionar gasto na aba {} para o mês {}", sheetName, month, e);
            throw e;
        }
    }

    /**
     * Atualiza um gasto existente na planilha.
     * Atualiza a descrição na coluna A e o valor na coluna do mês especificado.
     *
     * @param sheetName Nome da aba da planilha
     * @param month Nome do mês
     * @param rowId ID da linha (número da linha)
     * @param expense Objeto com os novos dados
     * @throws IOException Se houver erro ao comunicar com Google Sheets API
     */
    public void updateExpense(String sheetName, String month, int rowId, Expense expense) throws IOException {
        if (expense == null) {
             throw new IllegalArgumentException("Expense não pode ser nulo");
        }
        if (rowId < 2) {
            throw new IllegalArgumentException("ID da linha inválido: " + rowId);
        }

        String columnLetter = getColumnLetterForMonth(month);
        Sheets sheetsService = getSheetsService();

        try {
            // Atualiza a descrição na coluna A
            String descricaoRange = String.format("%s!A%d", sheetName, rowId);
            com.google.api.services.sheets.v4.model.ValueRange descricaoValueRange =
                new com.google.api.services.sheets.v4.model.ValueRange()
                    .setValues(Collections.singletonList(
                        Collections.singletonList(expense.getDescricao())
                    ));

            sheetsService.spreadsheets().values()
                .update(spreadsheetId, descricaoRange, descricaoValueRange)
                .setValueInputOption("RAW")
                .execute();

            // Atualiza o valor na coluna do mês
            String valorRange = String.format("%s!%s%d", sheetName, columnLetter, rowId);
            com.google.api.services.sheets.v4.model.ValueRange valorValueRange =
                new com.google.api.services.sheets.v4.model.ValueRange()
                    .setValues(Collections.singletonList(
                        Collections.singletonList(expense.getValor())
                    ));

            sheetsService.spreadsheets().values()
                .update(spreadsheetId, valorRange, valorValueRange)
                .setValueInputOption("RAW")
                .execute();

            logger.info("Gasto na linha {} atualizado com sucesso", rowId);
        } catch (IOException e) {
            logger.error("Erro ao atualizar gasto na linha {}", rowId, e);
            throw e;
        }
    }

    /**
     * Deleta a linha de um gasto na planilha.
     *
     * @param sheetName Nome da aba
     * @param rowId ID da linha a ser removida
     * @throws IOException Se houver erro ao comunicar com Google Sheets API
     */
    public void deleteExpense(String sheetName, int rowId) throws IOException {
        if (rowId < 2) {
             throw new IllegalArgumentException("ID da linha inválido: " + rowId);
        }

        Integer sheetId = getSheetId(sheetName);
        if (sheetId == null) {
            throw new IllegalArgumentException("Aba não encontrada: " + sheetName);
        }

        Sheets sheetsService = getSheetsService();

        try {
            BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
            DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                .setRange(new DimensionRange()
                    .setSheetId(sheetId)
                    .setDimension("ROWS")
                    .setStartIndex(rowId - 1) // 0-based index inclusive
                    .setEndIndex(rowId)       // 0-based index exclusive (so deletes 1 row)
                );

            Request request = new Request().setDeleteDimension(deleteRequest);
            batchUpdateRequest.setRequests(Collections.singletonList(request));

            sheetsService.spreadsheets()
                .batchUpdate(spreadsheetId, batchUpdateRequest)
                .execute();

            logger.info("Linha {} removida com sucesso da aba {}", rowId, sheetName);

        } catch (IOException e) {
            logger.error("Erro ao remover linha {} da aba {}", rowId, sheetName, e);
            throw e;
        }
    }

    public boolean isSpreadsheetAccessible() {
        try {
            Sheets sheetsService = getSheetsService();
            sheetsService.spreadsheets().get(spreadsheetId);
            return true;
        } catch (Exception e) {
            logger.error("Erro ao acessar a planilha: {}", e.getMessage());
            return false;
        }
    }
}
