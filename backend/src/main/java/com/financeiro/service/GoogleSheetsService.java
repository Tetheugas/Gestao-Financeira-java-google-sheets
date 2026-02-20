package com.financeiro.service;

import com.financeiro.exception.GoogleSheetsAuthException;
import com.financeiro.model.Expense;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço responsável pela integração com Google Sheets API v4.
 * Gerencia autenticação OAuth2 e operações de leitura/escrita na planilha.
 */
@Service
public class GoogleSheetsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleSheetsService.class);
    private static final String APPLICATION_NAME = "Gestao Financeira Pessoal";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String SPREADSHEET_NAME = "Gestão Financeira Pessoal";

    private final OAuth2AuthorizedClientService authorizedClientService;
    private NetHttpTransport httpTransport;

    // Cache to store Spreadsheet ID per user (Key: User Email, Value: Spreadsheet ID)
    private final Map<String, String> userSpreadsheetCache = new ConcurrentHashMap<>();

    public GoogleSheetsService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            logger.error("Failed to initialize GoogleSheetsService", e);
            throw e;
        }
    }

    /**
     * Helper to retrieve the current OAuth2 Authorized Client.
     */
    private OAuth2AuthorizedClient getAuthorizedClient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            return authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName());
        }
        return null;
    }

    /**
     * Checks if the current user is authorized.
     */
    public boolean isAuthorized() {
        return getAuthorizedClient() != null;
    }

    /**
     * Obtém uma instância autenticada do serviço Google Sheets.
     */
    private Sheets getSheetsService() throws GoogleSheetsAuthException {
        try {
            OAuth2AuthorizedClient client = getAuthorizedClient();
            if (client == null) {
                throw new GoogleSheetsAuthException("User not authenticated. Please login.");
            }
            String accessToken = client.getAccessToken().getTokenValue();

            HttpRequestInitializer requestInitializer = request -> {
                request.getHeaders().setAuthorization("Bearer " + accessToken);
            };
            
            return new Sheets.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
                    
        } catch (Exception e) {
            logger.error("Erro ao criar serviço Sheets", e);
            throw new GoogleSheetsAuthException("Erro ao criar serviço Sheets", e);
        }
    }

    /**
     * Obtém uma instância autenticada do serviço Google Drive.
     */
    private Drive getDriveService() throws GoogleSheetsAuthException {
        try {
            OAuth2AuthorizedClient client = getAuthorizedClient();
            if (client == null) {
                throw new GoogleSheetsAuthException("User not authenticated. Please login.");
            }
            String accessToken = client.getAccessToken().getTokenValue();

            HttpRequestInitializer requestInitializer = request -> {
                request.getHeaders().setAuthorization("Bearer " + accessToken);
            };

            return new Drive.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

        } catch (Exception e) {
            logger.error("Erro ao criar serviço Drive", e);
            throw new GoogleSheetsAuthException("Erro ao criar serviço Drive", e);
        }
    }

    /**
     * Resolve o ID da planilha para o usuário atual.
     * Procura no cache, depois no Drive, e se não achar, cria.
     */
    private String resolveSpreadsheetId() throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new GoogleSheetsAuthException("User not authenticated");
        }
        String userEmail = authentication.getName(); // Usually the email or sub

        // 1. Check Cache
        if (userSpreadsheetCache.containsKey(userEmail)) {
            return userSpreadsheetCache.get(userEmail);
        }

        // 2. Search in Drive
        logger.info("Searching for spreadsheet '{}' in Drive for user {}", SPREADSHEET_NAME, userEmail);
        Drive driveService = getDriveService();
        String query = "name = '" + SPREADSHEET_NAME + "' and mimeType = 'application/vnd.google-apps.spreadsheet' and trashed = false";

        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        List<File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            // Found it
            String id = files.get(0).getId();
            logger.info("Spreadsheet found with ID: {}", id);
            userSpreadsheetCache.put(userEmail, id);
            return id;
        }

        // 3. Create New
        logger.info("Spreadsheet not found. Creating new one...");
        return createNewSpreadsheet(); // This method will update the cache
    }

    public String createNewSpreadsheet() throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Sheets service = getSheetsService();
        Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties().setTitle(SPREADSHEET_NAME));

        Spreadsheet created = service.spreadsheets().create(spreadsheet).execute();
        String spreadsheetId = created.getSpreadsheetId();

        // Update cache
        userSpreadsheetCache.put(userEmail, spreadsheetId);

        logger.info("Nova planilha criada com ID: {}", spreadsheetId);
        return spreadsheetId;
    }

    // --- Original Methods Adapted ---

    // The rest of the methods (getSpreadsheetId, setSpreadsheetId) need adaptation or removal.
    // getSpreadsheetId public accessor is tricky because it depends on user context.

    public String getSpreadsheetId() {
        try {
            return resolveSpreadsheetId();
        } catch (IOException e) {
            logger.error("Could not resolve spreadsheet ID", e);
            return null;
        }
    }

    // Note: setSpreadsheetId is less relevant now as we auto-discover,
    // but if the user wants to switch spreadsheets, we can support it.
    public void setSpreadsheetId(String id) throws IOException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da planilha não pode ser vazio");
        }

        // Verifica acesso
        Sheets service = getSheetsService();
        try {
            service.spreadsheets().get(id).execute();
        } catch (IOException e) {
            logger.error("Erro ao acessar planilha com ID: " + id, e);
            throw new IllegalArgumentException("Não foi possível acessar a planilha. Verifique permissões.", e);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            userSpreadsheetCache.put(authentication.getName(), id);
        }
    }


    private String getColumnLetterForMonth(String month) {
        if (month == null || month.trim().isEmpty()) {
            throw new IllegalArgumentException("Mês não pode ser nulo ou vazio");
        }
        String normalizedMonth = month.trim().substring(0, 1).toUpperCase() +
                                 month.trim().substring(1).toLowerCase();

        switch (normalizedMonth) {
            case "Janeiro": return "B";
            case "Fevereiro": return "C";
            case "Março": return "D";
            case "Marco": return "D";
            case "Abril": return "E";
            case "Maio": return "F";
            case "Junho": return "G";
            case "Julho": return "H";
            case "Agosto": return "I";
            case "Setembro": return "J";
            case "Outubro": return "K";
            case "Novembro": return "L";
            case "Dezembro": return "M";
            default:
                throw new IllegalArgumentException("Mês inválido: " + month);
        }
    }

    private int findNextEmptyRow(String sheetName, String columnLetter) throws IOException {
        Sheets sheetsService = getSheetsService();
        String currentSpreadsheetId = resolveSpreadsheetId();
        String range = String.format("%s!%s2:%s1000", sheetName, columnLetter, columnLetter);

        try {
            var result = sheetsService.spreadsheets().values()
                    .get(currentSpreadsheetId, range)
                    .execute();
            List<List<Object>> values = result.getValues();
            if (values == null || values.isEmpty()) return 2;
            for (int i = 0; i < values.size(); i++) {
                List<Object> row = values.get(i);
                if (row == null || row.isEmpty() || row.get(0) == null || row.get(0).toString().trim().isEmpty()) {
                    return i + 2;
                }
            }
            return values.size() + 2;
        } catch (IOException e) {
            logger.error("Erro ao buscar próxima linha vazia", e);
            throw e;
        }
    }

    private boolean sheetExists(String sheetName) throws GoogleSheetsAuthException {
        try {
            Sheets sheetsService = getSheetsService();
            String currentSpreadsheetId = resolveSpreadsheetId();
            Spreadsheet spreadsheet = sheetsService.spreadsheets()
                    .get(currentSpreadsheetId)
                    .setFields("sheets.properties.title")
                    .execute();
            if (spreadsheet.getSheets() == null) return false;
            for (Sheet sheet : spreadsheet.getSheets()) {
                if (sheet.getProperties().getTitle().equals(sheetName)) return true;
            }
            return false;
        } catch (IOException e) {
            logger.error("Erro ao verificar existência da aba", e);
            throw new GoogleSheetsAuthException("Erro ao verificar existência da aba", e);
        }
    }

    public void createSheet(String sheetName) throws GoogleSheetsAuthException, IOException {
        try {
            Sheets sheetsService = getSheetsService();
            String currentSpreadsheetId = resolveSpreadsheetId();
            BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
            AddSheetRequest addSheetRequest = new AddSheetRequest();
            SheetProperties sheetProperties = new SheetProperties();
            sheetProperties.setTitle(sheetName);
            addSheetRequest.setProperties(sheetProperties);
            Request request = new Request();
            request.setAddSheet(addSheetRequest);
            batchUpdateRequest.setRequests(Collections.singletonList(request));
            sheetsService.spreadsheets().batchUpdate(currentSpreadsheetId, batchUpdateRequest).execute();
            logger.info("Nova aba '{}' criada", sheetName);
            addMonthHeaders(sheetName);
        } catch (IOException e) {
            logger.error("Erro ao criar aba", e);
            throw e;
        }
    }

    private void addMonthHeaders(String sheetName) throws IOException, GoogleSheetsAuthException {
        Sheets sheetsService = getSheetsService();
        String currentSpreadsheetId = resolveSpreadsheetId();
        List<Object> headers = new ArrayList<>();
        headers.add("Descrição");
        headers.add("Janeiro"); headers.add("Fevereiro"); headers.add("Março"); headers.add("Abril");
        headers.add("Maio"); headers.add("Junho"); headers.add("Julho"); headers.add("Agosto");
        headers.add("Setembro"); headers.add("Outubro"); headers.add("Novembro"); headers.add("Dezembro");
        String headerRange = String.format("%s!A1:M1", sheetName);
        ValueRange headerValueRange = new ValueRange().setValues(Collections.singletonList(headers));
        sheetsService.spreadsheets().values().update(currentSpreadsheetId, headerRange, headerValueRange)
                .setValueInputOption("RAW").execute();
    }

    public void ensureSheetExists(String sheetName) throws GoogleSheetsAuthException, IOException {
        if (!sheetExists(sheetName)) {
            createSheet(sheetName);
        }
    }

    public List<String> getSheetNames() throws IOException {
        try {
            Sheets sheetsService = getSheetsService();
            String currentSpreadsheetId = resolveSpreadsheetId();
            Spreadsheet spreadsheet = sheetsService.spreadsheets()
                    .get(currentSpreadsheetId)
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

    private Integer getSheetId(String sheetName) throws IOException {
        try {
            Sheets sheetsService = getSheetsService();
            String currentSpreadsheetId = resolveSpreadsheetId();
            Spreadsheet spreadsheet = sheetsService.spreadsheets()
                    .get(currentSpreadsheetId)
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
            logger.error("Erro ao buscar ID da aba", e);
            throw e;
        }
    }

    public List<Expense> readExpenses(String sheetName, String month) throws IOException {
        String columnLetter = getColumnLetterForMonth(month);
        ensureSheetExists(sheetName);
        Sheets sheetsService = getSheetsService();
        String currentSpreadsheetId = resolveSpreadsheetId();
        String range = String.format("%s!A2:%s1000", sheetName, columnLetter);
        try {
            var result = sheetsService.spreadsheets().values()
                    .get(currentSpreadsheetId, range)
                    .setValueRenderOption("UNFORMATTED_VALUE")
                    .execute();
            List<List<Object>> values = result.getValues();
            if (values == null || values.isEmpty()) return Collections.emptyList();
            List<Expense> expenses = new ArrayList<>();
            int monthColumnIndex = columnLetter.charAt(0) - 'A';
            for (int i = 0; i < values.size(); i++) {
                List<Object> row = values.get(i);
                if (row == null || row.isEmpty()) continue;
                String descricao = row.size() > 0 && row.get(0) != null ? row.get(0).toString().trim() : "";
                if (descricao.isEmpty()) continue;
                Object cellValue = row.size() > monthColumnIndex ? row.get(monthColumnIndex) : null;
                if (cellValue == null || cellValue.toString().trim().isEmpty()) continue;
                try {
                    Double valor;
                    if (cellValue instanceof Number) {
                        valor = ((Number) cellValue).doubleValue();
                    } else {
                        String valorStr = cellValue.toString().trim();
                        try {
                            valor = Double.parseDouble(valorStr);
                        } catch (NumberFormatException e) {
                            String cleanValue = valorStr.replaceAll("[^\\d.,-]", "").trim();
                            if (cleanValue.contains(",") && cleanValue.contains(".")) {
                                cleanValue = cleanValue.replace(".", "").replace(",", ".");
                            } else if (cleanValue.contains(",")) {
                                cleanValue = cleanValue.replace(",", ".");
                            }
                            valor = Double.parseDouble(cleanValue);
                        }
                    }
                    int rowId = i + 2;
                    expenses.add(new Expense(rowId, descricao, valor));
                } catch (NumberFormatException e) {
                    logger.warn("Valor inválido: {}", cellValue);
                }
            }
            return expenses;
        } catch (IOException e) {
            logger.error("Erro ao ler gastos", e);
            throw e;
        }
    }

    public void addExpense(String sheetName, String month, Expense expense) throws IOException {
        if (expense == null || expense.getDescricao() == null || expense.getDescricao().trim().isEmpty() || expense.getValor() == null) {
            throw new IllegalArgumentException("Dados do gasto inválidos");
        }
        String columnLetter = getColumnLetterForMonth(month);
        ensureSheetExists(sheetName);
        int nextRow = findNextEmptyRow(sheetName, "A");
        Sheets sheetsService = getSheetsService();
        String currentSpreadsheetId = resolveSpreadsheetId();
        try {
            String descricaoRange = String.format("%s!A%d", sheetName, nextRow);
            ValueRange descricaoValueRange = new ValueRange().setValues(Collections.singletonList(Collections.singletonList(expense.getDescricao())));
            sheetsService.spreadsheets().values().update(currentSpreadsheetId, descricaoRange, descricaoValueRange).setValueInputOption("RAW").execute();
            String valorRange = String.format("%s!%s%d", sheetName, columnLetter, nextRow);
            ValueRange valorValueRange = new ValueRange().setValues(Collections.singletonList(Collections.singletonList(expense.getValor())));
            sheetsService.spreadsheets().values().update(currentSpreadsheetId, valorRange, valorValueRange).setValueInputOption("RAW").execute();
            logger.info("Gasto adicionado: {} - {}", expense.getDescricao(), expense.getValor());
        } catch (IOException e) {
            logger.error("Erro ao adicionar gasto", e);
            throw e;
        }
    }

    public void updateExpense(String sheetName, String month, int rowId, Expense expense) throws IOException {
        if (expense == null) throw new IllegalArgumentException("Expense não pode ser nulo");
        if (rowId < 2) throw new IllegalArgumentException("ID inválido");
        String columnLetter = getColumnLetterForMonth(month);
        Sheets sheetsService = getSheetsService();
        String currentSpreadsheetId = resolveSpreadsheetId();
        try {
            String descricaoRange = String.format("%s!A%d", sheetName, rowId);
            ValueRange descricaoValueRange = new ValueRange().setValues(Collections.singletonList(Collections.singletonList(expense.getDescricao())));
            sheetsService.spreadsheets().values().update(currentSpreadsheetId, descricaoRange, descricaoValueRange).setValueInputOption("RAW").execute();
            String valorRange = String.format("%s!%s%d", sheetName, columnLetter, rowId);
            ValueRange valorValueRange = new ValueRange().setValues(Collections.singletonList(Collections.singletonList(expense.getValor())));
            sheetsService.spreadsheets().values().update(currentSpreadsheetId, valorRange, valorValueRange).setValueInputOption("RAW").execute();
        } catch (IOException e) {
            logger.error("Erro ao atualizar gasto", e);
            throw e;
        }
    }

    public void renameSheet(String oldName, String newName) throws IOException {
        if (oldName == null || newName == null) throw new IllegalArgumentException("Nomes inválidos");
        Integer sheetId = getSheetId(oldName);
        if (sheetId == null) throw new IllegalArgumentException("Aba não encontrada: " + oldName);
        Sheets sheetsService = getSheetsService();
        String currentSpreadsheetId = resolveSpreadsheetId();
        try {
            BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
            UpdateSheetPropertiesRequest updateRequest = new UpdateSheetPropertiesRequest()
                    .setProperties(new SheetProperties().setSheetId(sheetId).setTitle(newName))
                    .setFields("title");
            batchUpdateRequest.setRequests(Collections.singletonList(new Request().setUpdateSheetProperties(updateRequest)));
            sheetsService.spreadsheets().batchUpdate(currentSpreadsheetId, batchUpdateRequest).execute();
        } catch (IOException e) {
            logger.error("Erro ao renomear aba", e);
            throw e;
        }
    }

    public void deleteSheet(String sheetName) throws IOException {
        if (sheetName == null) throw new IllegalArgumentException("Nome inválido");
        Integer sheetId = getSheetId(sheetName);
        if (sheetId == null) throw new IllegalArgumentException("Aba não encontrada");
        Sheets sheetsService = getSheetsService();
        String currentSpreadsheetId = resolveSpreadsheetId();
        try {
            BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
            DeleteSheetRequest deleteRequest = new DeleteSheetRequest().setSheetId(sheetId);
            batchUpdateRequest.setRequests(Collections.singletonList(new Request().setDeleteSheet(deleteRequest)));
            sheetsService.spreadsheets().batchUpdate(currentSpreadsheetId, batchUpdateRequest).execute();
        } catch (IOException e) {
            logger.error("Erro ao excluir aba", e);
            throw e;
        }
    }

    public void deleteExpense(String sheetName, int rowId) throws IOException {
        if (rowId < 2) throw new IllegalArgumentException("ID inválido");
        Integer sheetId = getSheetId(sheetName);
        if (sheetId == null) throw new IllegalArgumentException("Aba não encontrada");
        Sheets sheetsService = getSheetsService();
        String currentSpreadsheetId = resolveSpreadsheetId();
        try {
            BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
            DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                .setRange(new DimensionRange().setSheetId(sheetId).setDimension("ROWS").setStartIndex(rowId - 1).setEndIndex(rowId));
            batchUpdateRequest.setRequests(Collections.singletonList(new Request().setDeleteDimension(deleteRequest)));
            sheetsService.spreadsheets().batchUpdate(currentSpreadsheetId, batchUpdateRequest).execute();
        } catch (IOException e) {
            logger.error("Erro ao remover linha", e);
            throw e;
        }
    }

    public boolean isSpreadsheetAccessible() {
        try {
            resolveSpreadsheetId(); // This checks auth and existence
            return true;
        } catch (Exception e) {
            logger.error("Erro ao acessar a planilha: {}", e.getMessage());
            return false;
        }
    }
}
