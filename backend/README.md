# Gestão Financeira Pessoal - Backend

Backend Spring Boot para sistema de gestão financeira pessoal usando Google Sheets como armazenamento.

## Requisitos

- Java 17 ou superior
- Maven 3.6+
- Conta Google com acesso ao Google Sheets
- Credenciais OAuth2 do Google Cloud

## Configuração do Google Cloud

### 1. Criar Projeto no Google Cloud Console

1. Acesse [Google Cloud Console](https://console.cloud.google.com/)
2. Clique em "Criar Projeto"
3. Dê um nome ao projeto (ex: "gestao-financeira-pessoal")
4. Clique em "Criar"

### 2. Habilitar Google Sheets API v4

1. No menu lateral, vá em "APIs e Serviços" > "Biblioteca"
2. Busque por "Google Sheets API"
3. Clique em "Google Sheets API"
4. Clique em "Ativar"

### 3. Criar Credenciais OAuth2

1. No menu lateral, vá em "APIs e Serviços" > "Credenciais"
2. Clique em "Criar Credenciais" > "ID do cliente OAuth"
3. Se solicitado, configure a tela de consentimento OAuth:
   - Tipo de usuário: Externo
   - Preencha os campos obrigatórios
   - Adicione escopo: `https://www.googleapis.com/auth/spreadsheets`
4. Tipo de aplicativo: "Aplicativo para computador"
5. Nome: "Gestão Financeira Backend"
6. Clique em "Criar"
7. Baixe o arquivo JSON de credenciais
8. Renomeie o arquivo para `credentials.json`
9. Coloque o arquivo na raiz do projeto backend (mesmo nível do pom.xml)

**IMPORTANTE:** O arquivo `credentials.json` contém informações sensíveis e NÃO deve ser commitado no Git. Ele já está incluído no `.gitignore`.

### 4. Configurar ID da Planilha

1. Crie uma planilha no Google Sheets ou use uma existente
2. A URL da planilha tem o formato: `https://docs.google.com/spreadsheets/d/SPREADSHEET_ID/edit`
3. Copie o `SPREADSHEET_ID` da URL
4. Edite o arquivo `src/main/resources/application.properties`
5. Substitua `YOUR_SPREADSHEET_ID_HERE` pelo ID copiado

## Estrutura da Planilha

A planilha deve seguir esta estrutura:

```
Aba: "CartãoNubank" (ou qualquer nome)

     A              B           C          D
1  Descrição    Fevereiro    Março      Abril
2  Netflix      45.90       45.90      45.90
3  Uber         32.50       28.00      41.20
4  Mercado      450.00      520.00     480.00
```

- Coluna A: Descrições dos gastos
- Colunas B+: Valores numéricos por mês (sem formatação de moeda)
- Linha 1: Headers com nomes dos meses

## Executar o Projeto

### Compilar

```bash
mvn clean install
```

### Executar

```bash
mvn spring-boot:run
```

O servidor iniciará na porta 8080.

### Primeira Execução

Na primeira execução, uma janela do navegador abrirá solicitando autorização para acessar sua conta Google. Após autorizar, um token será salvo localmente em `tokens/` e você não precisará autorizar novamente.

## Endpoints da API

### GET /api/expenses/{mes}?aba={abaName}

Retorna lista de gastos de um mês específico.

**Exemplo:**
```bash
curl "http://localhost:8080/api/expenses/Fevereiro?aba=CartãoNubank"
```

**Resposta:**
```json
[
  {
    "descricao": "Netflix",
    "valorFormatado": "R$ 45,90"
  },
  {
    "descricao": "Uber",
    "valorFormatado": "R$ 32,50"
  }
]
```

### POST /api/expenses

Adiciona um novo gasto.

**Exemplo:**
```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "descricao": "Spotify",
    "valor": 19.90,
    "aba": "CartãoNubank",
    "mes": "Fevereiro"
  }'
```

**Resposta:**
```json
{
  "message": "Gasto adicionado com sucesso",
  "status": "success"
}
```

## Testes

### Executar todos os testes

```bash
mvn test
```

### Executar apenas testes unitários

```bash
mvn test -Dtest="*Test"
```

### Executar apenas testes de propriedade

```bash
mvn test -Dtest="*Properties"
```

## Estrutura do Projeto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/financeiro/
│   │   │   ├── controller/     # REST Controllers
│   │   │   ├── service/        # Business Logic
│   │   │   ├── model/          # Domain Models
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── util/           # Utility Classes
│   │   │   └── exception/      # Custom Exceptions
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/financeiro/
├── pom.xml
└── README.md
```

## Tecnologias

- Spring Boot 3.2.0
- Java 17
- Google Sheets API v4
- Maven
- jqwik (Property-Based Testing)
- JUnit 5

## Troubleshooting

### Erro: "credentials.json not found"

Certifique-se de que o arquivo `credentials.json` está na raiz do projeto backend.

### Erro: "The caller does not have permission"

Verifique se a Google Sheets API está habilitada no Google Cloud Console e se você autorizou o aplicativo.

### Erro: "Spreadsheet not found"

Verifique se o ID da planilha em `application.properties` está correto e se você tem acesso à planilha.
