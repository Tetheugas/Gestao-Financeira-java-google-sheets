# Gestão Financeira Pessoal

Sistema de gestão financeira pessoal composto por um backend em Java (Spring Boot) e um frontend em React (Vite + TypeScript), utilizando o Google Sheets como banco de dados.

## Pré-requisitos

Certifique-se de ter os seguintes softwares instalados:

*   **Java 17+** (para o backend)
*   **Maven 3.6+** (para o backend)
*   **Node.js 18+ e npm** (para o frontend)
*   **Conta Google** (para acesso ao Google Sheets e Drive)

## Configuração do Backend

O backend utiliza OAuth2 para autenticar usuários e acessar suas planilhas no Google Drive.

### 1. Configurar Projeto no Google Cloud

1.  Acesse o [Google Cloud Console](https://console.cloud.google.com/).
2.  Crie um novo projeto.
3.  No menu "APIs e Serviços" > "Biblioteca", procure e ative as seguintes APIs:
    *   **Google Sheets API**
    *   **Google Drive API**
4.  No menu "APIs e Serviços" > "Tela de permissão OAuth":
    *   Escolha "Externo" (ou Interno se for usuário G Suite).
    *   Preencha os dados obrigatórios.
    *   Adicione o seu email como "Usuário de teste" (se o app estiver em modo de teste).
5.  No menu "APIs e Serviços" > "Credenciais":
    *   Clique em "Criar Credenciais" > "ID do cliente OAuth".
    *   Tipo de aplicativo: **Aplicação da Web**.
    *   Em "Origens JavaScript autorizadas", adicione: `http://localhost:8080` e `http://localhost:5173`.
    *   Em "URIs de redirecionamento autorizados", adicione: `http://localhost:8080/login/oauth2/code/google`.
    *   Clique em "Criar".
    *   **Copie o "ID do cliente" e a "Chave secreta do cliente".**

### 2. Configurar `application.properties`

1.  Navegue até a pasta `backend/src/main/resources`.
2.  Copie o arquivo de exemplo para o arquivo real:
    ```bash
    cp backend/src/main/resources/application.properties.example backend/src/main/resources/application.properties
    ```
    *(Nota: Se estiver no Windows, copie e renomeie o arquivo manualmente)*
3.  Edite o arquivo `backend/src/main/resources/application.properties` e preencha as credenciais:
    ```properties
    spring.security.oauth2.client.registration.google.client-id=SEU_CLIENT_ID_AQUI
    spring.security.oauth2.client.registration.google.client-secret=SUA_CLIENT_SECRET_AQUI
    ```

## Configuração do Frontend

1.  Navegue até a pasta `frontend`.
2.  Instale as dependências:
    ```bash
    cd frontend
    npm install
    ```
3.  Copie o arquivo de exemplo de ambiente:
    ```bash
    cp .env.example .env
    ```
    *(Geralmente não é necessário alterar o `VITE_API_URL` se estiver rodando localmente na porta padrão)*

## Como Rodar o Projeto

### Backend

Em um terminal, navegue até a pasta `backend` e execute:

```bash
cd backend
mvn spring-boot:run
```

O servidor iniciará na porta `8080`.

### Frontend

Em **outro** terminal, navegue até a pasta `frontend` e execute:

```bash
cd frontend
npm run dev
```

O frontend estará disponível em `http://localhost:5173`.

## Uso

1.  Abra `http://localhost:5173` no navegador.
2.  Clique no botão de Login.
3.  Você será redirecionado para o Google para autorizar o acesso.
4.  Após autorizar, o sistema criará automaticamente uma planilha "Gestão Financeira Pessoal" no seu Google Drive (se não existir) e você poderá gerenciar seus gastos.

## Troubleshooting

### Erro: `UnsatisfiedDependencyException` / `Parameter 0 of constructor in GoogleSheetsService required a bean of type OAuth2AuthorizedClientService`

Este erro ocorre quando o Spring Boot não encontra uma configuração OAuth2 válida e, portanto, não inicializa o serviço de cliente OAuth2.
**Solução:** Certifique-se de que você criou o arquivo `backend/src/main/resources/application.properties` e preencheu corretamente o `client-id` e `client-secret` conforme descrito na seção "Configuração do Backend".

### Erro de permissão ou "Access Blocked: App has not completed the Google verification process"

Se você vir uma tela de erro do Google dizendo que o app não é verificado, clique em "Advanced" (Avançado) e depois em "Go to... (unsafe)" para prosseguir (apenas para desenvolvimento). Certifique-se também de ter adicionado seu email como "Usuário de teste" na Tela de Consentimento OAuth no Google Cloud Console.
