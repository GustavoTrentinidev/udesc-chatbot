# Manual Setup Guide

Steps you need to complete before the WhatsApp bot works end-to-end.

---

## 1. Meta Developer Account

1. Go to [developers.facebook.com](https://developers.facebook.com) and log in with a Facebook account
2. Click **My Apps → Create App → Business**
3. Give it any name (e.g. `UDESC Chatbot`) and click **Create**
4. On the app dashboard, find **WhatsApp** and click **Set Up**

---

## 2. Get Your Credentials

From the WhatsApp setup page in Meta Developer Console:

| Credential | Where to find it |
|---|---|
| **Phone Number ID** | WhatsApp → API Setup → Phone number ID |
| **Access Token** | Create a permanent System User token (see below) |
| **Verify Token** | You choose this — any secret string |

### Creating a permanent System User token (won't expire)
1. Go to **Business Settings → System Users → Add**
2. Create a system user with **Employee** role
3. Click **Generate New Token**, select your app, grant `whatsapp_business_messaging` permission
4. Copy the token — it won't be shown again

---

## 3. Create Your .env File

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

Edit `.env`:
```
POSTGRES_DB=chatbot
POSTGRES_USER=chatbot
POSTGRES_PASSWORD=chatbot

WHATSAPP_PHONE_NUMBER_ID=<your phone number ID>
WHATSAPP_ACCESS_TOKEN=<your system user token>
WHATSAPP_VERIFY_TOKEN=<any secret string you choose>
```

---

## 4. Set Up ngrok Static Domain

1. Create a free account at [ngrok.com](https://ngrok.com)
2. Install ngrok and authenticate: `ngrok config add-authtoken <your-token>`
3. Go to [dashboard.ngrok.com/domains](https://dashboard.ngrok.com/domains) → **New Domain**
4. You'll get a free static domain like `your-name.ngrok-free.app`
5. Save that domain — you'll use it every time

**Start ngrok (run this before starting the bot):**
```bash
ngrok http --domain=your-name.ngrok-free.app 8080
```

---

## 5. Register the Webhook (one time only)

1. In Meta Developer Console → **WhatsApp → Configuration → Webhook**
2. Click **Edit** and fill in:
   - **Callback URL:** `https://your-name.ngrok-free.app/webhook/whatsapp`
   - **Verify Token:** same value you set in `.env`
3. Click **Verify and Save**
4. Under **Webhook fields**, subscribe to **messages**

> This only needs to be done once. Since you're using a static ngrok domain, the URL never changes.

---

## 6. Add Test Phone Numbers (Sandbox)

1. In Meta Developer Console → **WhatsApp → API Setup**
2. Under **To**, add your personal WhatsApp number as a recipient
3. You can add up to 5 numbers for free testing

---

## 7. Start the Bot

Every time you want to run the bot:

```bash
# Terminal 1 — start ngrok tunnel
ngrok http --domain=your-name.ngrok-free.app 8080

# Terminal 2 — start the backend + database
docker compose up --build
```

Wait for: `Started ChatbotApplication in X seconds`

---

## 8. Test It

### 8.1 Health check (before touching WhatsApp)

Open a browser or run:
```bash
curl http://localhost:8080/actuator/health
```
Expected response:
```json
{"status":"UP","components":{"db":{"status":"UP"},"ping":{"status":"UP"}}}
```
If `db` is not `UP`, wait a few seconds and try again — PostgreSQL may still be initializing.

---

### 8.2 Test the REST API directly (no WhatsApp needed)

You can fully test the chatbot logic before touching WhatsApp using curl or the Postman collection (`UDESC-Chatbot.postman_collection.json`).

**Start a session:**
```bash
curl -s -X POST http://localhost:8080/api/v1/sessions | python3 -m json.tool
```
Expected: `nodeId: "language-select"` with 3 language options.

**Select English (optionIndex 0):**
```bash
SESSION_ID=<paste sessionId from above>

curl -s -X POST http://localhost:8080/api/v1/sessions/$SESSION_ID/select \
  -H "Content-Type: application/json" \
  -d '{"optionIndex": 0}' | python3 -m json.tool
```
Expected: `nodeId: "menu-main"` with all 8 menu options.

**Navigate to Campus Addresses → CCT:**
```bash
# Select "Campus Addresses" (index 0)
curl -s -X POST http://localhost:8080/api/v1/sessions/$SESSION_ID/select \
  -H "Content-Type: application/json" -d '{"optionIndex": 0}'

# Select CCT Joinville (index 0)
curl -s -X POST http://localhost:8080/api/v1/sessions/$SESSION_ID/select \
  -H "Content-Type: application/json" -d '{"optionIndex": 0}'
```
Expected: CCT address and website URL.

**Test invalid option (expect 400):**
```bash
curl -s -X POST http://localhost:8080/api/v1/sessions/$SESSION_ID/select \
  -H "Content-Type: application/json" -d '{"optionIndex": 99}'
```
Expected: `{"error":"Invalid option index 99...","timestamp":"..."}`

**Test unknown session (expect 404):**
```bash
curl -s http://localhost:8080/api/v1/sessions/00000000-0000-0000-0000-000000000000
```
Expected: `{"error":"Session not found: ...","timestamp":"..."}`

---

### 8.3 Test the webhook verification

Confirm Meta would accept your webhook registration:
```bash
curl -s "http://localhost:8080/webhook/whatsapp\
?hub.mode=subscribe\
&hub.verify_token=<your WHATSAPP_VERIFY_TOKEN>\
&hub.challenge=testchallenge"
```
Expected response: `testchallenge` (the challenge echoed back).

With a wrong token:
```bash
curl -s -o /dev/null -w "%{http_code}" \
  "http://localhost:8080/webhook/whatsapp?hub.mode=subscribe&hub.verify_token=wrong&hub.challenge=x"
```
Expected: `403`

---

### 8.4 Test on WhatsApp

Once ngrok is running and the webhook is registered in Meta Developer Console:

1. Open WhatsApp on your phone and find the bot's number (shown in Meta Developer Console under **API Setup**)
2. Send any text message (e.g. `"hi"`)
3. You should receive the welcome message with 3 language buttons:
   - 🇺🇸 English
   - 🇧🇷 Português
   - 🇪🇸 Español
4. Tap a language → you should receive the main menu as a **list message** (tap "See options" to expand)
5. Select any topic and verify the content is correct
6. Navigate back and try a different topic
7. Send a free-text message at any point → the bot should re-send the current menu

**Full test path to cover all 8 menus:**

| Step | Action | Expected node |
|---|---|---|
| 1 | Send "hi" | Language select (3 buttons) |
| 2 | Tap English | Main menu (list, 8 options) |
| 3 | Select Campus Addresses | Campus list |
| 4 | Select CCT Joinville | Address + website |
| 5 | Back → Back → select UDESC Systems Access | SIGA / Moodle / SIGAA |
| 6 | Select SIGA | Access instructions |
| 7 | Back → Back → select How to Get UDESC ID | Step-by-step instructions |
| 8 | Back → select Systems Description | Systems list |
| 9 | Back → select CPF Information | CPF instructions |
| 10 | Back → select Tutoring Team | Contact info |
| 11 | Back → select SOE Services | SOE details |
| 12 | Back → select Student Housing | Housing info |

---

## Presentation Day Checklist

- [ ] `.env` file has real credentials
- [ ] ngrok is running with the static domain
- [ ] `docker compose up` shows `Started ChatbotApplication`
- [ ] `GET http://localhost:8080/actuator/health` returns `{"status":"UP"}`
- [ ] Sent a test message and received a response on WhatsApp
- [ ] Internet access confirmed in lab F205 (required for Meta webhook delivery)
