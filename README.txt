UDESC Chatbot Backend
=====================
Rule-based chatbot for foreign exchange students at UDESC.
Disciplina: Redes de Computadores – 2026/1

PREREQUISITES
-------------
- Docker (https://docs.docker.com/get-docker/)
- Docker Compose (included with Docker Desktop)

STARTUP (single command)
------------------------
  docker compose up --build

The first run will:
  1. Pull postgres:16-alpine image
  2. Build the Spring Boot application image
  3. Start PostgreSQL on port 5432
  4. Run Flyway migrations (creates chat_sessions table)
  5. Start the chatbot API on port 8080

Wait until you see:
  backend  | Started ChatbotApplication in X.XXX seconds

STOPPING
--------
  docker compose down

To also remove the database volume:
  docker compose down -v

NETWORK INFORMATION (for lab presentation)
-------------------------------------------
Protocol:    TCP (HTTP/REST over TCP)
Server port: 8080 (exposed to host and LAN)
DB port:     5432 (internal, exposed to host for debugging)

To find your machine's IP (so Android client can connect):
  Linux/macOS: ip addr   or   hostname -I
  Windows:     ipconfig

The Android client should connect to:
  http://<YOUR_LAN_IP>:8080

API ENDPOINTS
-------------
1. Start a new session (GET language selection):
   POST http://localhost:8080/api/v1/sessions
   Response: { "sessionId": "...", "nodeId": "language-select", "message": "...", "options": [...] }

2. Get current session state:
   GET http://localhost:8080/api/v1/sessions/{sessionId}

3. Send user selection (tap a button):
   POST http://localhost:8080/api/v1/sessions/{sessionId}/select
   Body: { "optionIndex": 0 }
   Response: { "sessionId": "...", "nodeId": "...", "message": "...", "options": [...] }

4. Health check:
   GET http://localhost:8080/actuator/health
   Response: { "status": "UP" }

EXAMPLE SESSION (curl)
----------------------
# 1. Start session
SESSION=$(curl -s -X POST http://localhost:8080/api/v1/sessions | python3 -c "import sys,json; print(json.load(sys.stdin)['sessionId'])")

# 2. Select English (optionIndex 0)
curl -s -X POST http://localhost:8080/api/v1/sessions/$SESSION/select \
  -H "Content-Type: application/json" \
  -d '{"optionIndex": 0}'

# 3. Select "Campus Addresses" (optionIndex 0)
curl -s -X POST http://localhost:8080/api/v1/sessions/$SESSION/select \
  -H "Content-Type: application/json" \
  -d '{"optionIndex": 0}'

# 4. Select CCT campus (optionIndex 0)
curl -s -X POST http://localhost:8080/api/v1/sessions/$SESSION/select \
  -H "Content-Type: application/json" \
  -d '{"optionIndex": 0}'

CHATBOT MENUS
-------------
Language selection → English / Português / Español
Main Menu:
  1. Campus Addresses       (CCT Joinville, FAED, CEFID)
  2. UDESC Systems Access   (SIGA, Moodle, SIGAA)
  3. How to Get UDESC ID    (step-by-step instructions)
  4. Systems Description    (SIGA, Moodle, Site, SAS, Office 365, Library)
  5. CPF Information        (online registration + Receita Federal)
  6. Tutoring Team          (CCT tutoria.cct@udesc.br)
  7. SOE Services           (Student Orientation Service)
  8. Student Housing        (CCT housing information)

ENVIRONMENT VARIABLES (optional override)
-----------------------------------------
Copy .env.example to .env and edit values:
  POSTGRES_DB=chatbot
  POSTGRES_USER=chatbot
  POSTGRES_PASSWORD=chatbot

RUNNING TESTS LOCALLY
---------------------
Requires: Java 21+ and Maven
  cd backend
  ./mvnw test

All 12 tests should pass (uses H2 in-memory database for tests).
