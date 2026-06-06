# UDESC Chatbot

Chatbot para auxiliar estudantes estrangeiros na UDESC, desenvolvido para a disciplina de Redes de Computadores (Prof. Janine Kniess).

O bot responde via WhatsApp (sandbox do Twilio) com informações sobre campi, sistemas, ID UDESC, CPF, tutoria, SOE e moradia.

---

## Configuração do arquivo .env

Antes de executar o projeto, você precisa criar um arquivo `.env` na raiz do repositório. Use o `.env.example` como base:

```bash
cp .env.example .env
```

Em seguida, preencha as variáveis:

```env
# Banco de dados (pode deixar os valores padrão)
POSTGRES_DB=chatbot
POSTGRES_USER=chatbot
POSTGRES_PASSWORD=chatbot

# Ngrok — necessário para expor o servidor localmente
# Crie uma conta em https://ngrok.com e copie o token em: Dashboard > Your Authtoken
NGROK_AUTH_TOKEN=seu_token_aqui
# Domínio fixo gratuito da sua conta ngrok. Encontre em: Dashboard > Domains.
NGROK_DEV_DOMAIN=seu_dominio.ngrok-free.app
NGROK_DEV_DOMAIN=

# Twilio WhatsApp Sandbox
# 1. Crie uma conta gratuita em https://www.twilio.com (não precisa de cartão)
# 2. Na página inicial do console (https://console.twilio.com) copie o Account SID e o Auth Token
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=seu_auth_token_aqui
# Número do sandbox do Twilio no formato whatsapp:+<número>.
# Encontre em: Twilio Console → Messaging → Try it out → Send a WhatsApp message
TWILIO_FROM_NUMBER=whatsapp:+seu_numero_sandbox
```

> **O arquivo `.env` nunca deve ser commitado.** Ele já está no `.gitignore`.

---

## Como executar

Com o `.env` preenchido, basta rodar na raiz do projeto:

```bash
./run.sh
```

O script vai:
- Verificar e instalar o Java 21 se necessário
- Verificar e instalar o Docker e Docker Compose se necessário
- Verificar e instalar o ngrok se necessário
- Subir a aplicação via Docker Compose
- Iniciar o túnel ngrok automaticamente

Após iniciar, configure a URL do webhook no Twilio:

**Twilio Console → Messaging → Try it out → Send a WhatsApp message → Sandbox Settings**

- **WHEN A MESSAGE COMES IN:** `https://<url-do-ngrok>/webhook/twilio` — HTTP POST

---

## Como testar pelo WhatsApp

1. Envie a mensagem de adesão ao sandbox do Twilio para o número **+1 415 523 8886**:
   ```
   join <palavra>-<palavra>
   ```
   *(o código aparece na página do sandbox no console do Twilio)*

2. Após a confirmação, envie qualquer mensagem para o mesmo número — o bot vai responder com o menu.

3. Navegue respondendo com o **número** da opção desejada (`1`, `2`, etc.).
