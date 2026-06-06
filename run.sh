#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAVA_MIN_VERSION=21

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }
step()  { echo -e "\n${CYAN}==>${NC} $1"; }

# ─── .env ────────────────────────────────────────────────────────────────────

load_env() {
    step "Loading .env"
    [ -f "$SCRIPT_DIR/.env" ] || error ".env not found at $SCRIPT_DIR/.env — add it before running this script."
    set -a
    # shellcheck disable=SC1091
    source "$SCRIPT_DIR/.env"
    set +a
    info ".env loaded"
}

# ─── Java ────────────────────────────────────────────────────────────────────

check_java() {
    step "Checking Java"
    if command -v java &>/dev/null; then
        local ver
        ver=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
        if [ "${ver:-0}" -ge "$JAVA_MIN_VERSION" ] 2>/dev/null; then
            info "Java $ver found"
            return
        else
            warn "Java $ver found — Java $JAVA_MIN_VERSION+ required"
        fi
    else
        warn "Java not found"
    fi

    info "Installing OpenJDK $JAVA_MIN_VERSION..."
    sudo apt-get update -qq
    sudo apt-get install -y "openjdk-${JAVA_MIN_VERSION}-jdk"
    info "Java $JAVA_MIN_VERSION installed"
}

# ─── Docker ──────────────────────────────────────────────────────────────────

check_docker() {
    step "Checking Docker"
    if ! command -v docker &>/dev/null; then
        warn "Docker not found — installing..."
        curl -fsSL https://get.docker.com | sudo sh
        sudo usermod -aG docker "$USER"
        warn "Docker installed. Group change takes effect on next login."
    else
        info "Docker found: $(docker --version)"
    fi

    # Determine whether to use sudo for docker commands
    if docker info &>/dev/null 2>&1; then
        DOCKER="docker"
    else
        warn "Running Docker with sudo (user not in docker group yet)"
        DOCKER="sudo docker"
    fi

    if ! $DOCKER compose version &>/dev/null 2>&1; then
        warn "Docker Compose plugin not found — installing..."
        sudo apt-get update -qq
        sudo apt-get install -y docker-compose-plugin
    fi
    info "Docker Compose found: $($DOCKER compose version --short)"
}

# ─── ngrok ───────────────────────────────────────────────────────────────────

check_ngrok() {
    step "Checking ngrok"
    if ! command -v ngrok &>/dev/null; then
        warn "ngrok not found — installing..."
        curl -sSL https://ngrok-agent.s3.amazonaws.com/ngrok.asc \
            | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null
        echo "deb https://ngrok-agent.s3.amazonaws.com buster main" \
            | sudo tee /etc/apt/sources.list.d/ngrok.list >/dev/null
        sudo apt-get update -qq
        sudo apt-get install -y ngrok
        info "ngrok installed"
    else
        info "ngrok found: $(ngrok version)"
    fi

    if [ -n "${NGROK_AUTH_TOKEN:-}" ] && [ "$NGROK_AUTH_TOKEN" != "your_ngrok_auth_token_here" ]; then
        ngrok config add-authtoken "$NGROK_AUTH_TOKEN"
        info "ngrok auth token configured"
    else
        warn "NGROK_AUTH_TOKEN not set in .env — skipping token configuration"
    fi
}

# ─── Application ─────────────────────────────────────────────────────────────

start_app() {
    step "Starting application"
    cd "$SCRIPT_DIR"
    $DOCKER compose up --build -d
    info "Application running on http://localhost:8080"
}

# ─── ngrok tunnel ────────────────────────────────────────────────────────────

start_ngrok() {
    step "Starting ngrok tunnel"

    # Kill any existing ngrok process
    if pgrep -x ngrok &>/dev/null; then
        warn "Stopping existing ngrok process..."
        pkill -x ngrok || true
        sleep 1
    fi

    if [ -n "${NGROK_STATIC_URL:-}" ] && [ "$NGROK_STATIC_URL" != "your_ngrok_static_url" ]; then
        nohup ngrok http --domain="$NGROK_STATIC_URL" 8080 > /tmp/ngrok.log 2>&1 &
        info "ngrok tunnel: https://$NGROK_STATIC_URL"
    else
        nohup ngrok http 8080 > /tmp/ngrok.log 2>&1 &
        sleep 2
        local url
        url=$(curl -s http://localhost:4040/api/tunnels 2>/dev/null \
            | grep -oP 'https://[^"]+\.ngrok[^"]+' | head -1 || true)
        if [ -n "$url" ]; then
            info "ngrok tunnel: $url"
        else
            warn "ngrok started but could not detect URL — check http://localhost:4040"
        fi
    fi

    warn "Update the webhook URL in Twilio console if the URL changed:"
    echo "       Twilio > Messaging > Sandbox Settings > WHEN A MESSAGE COMES IN"
    echo "       https://<ngrok-url>/webhook/twilio  [HTTP POST]"
}

# ─── Main ────────────────────────────────────────────────────────────────────

main() {
    echo -e "${CYAN}"
    echo "  ╔══════════════════════════════════╗"
    echo "  ║     UDESC Chatbot — Lab Setup    ║"
    echo "  ╚══════════════════════════════════╝"
    echo -e "${NC}"

    load_env
    check_java
    check_docker
    check_ngrok
    start_app
    start_ngrok

    echo ""
    info "All done."
    echo ""
    echo -e "  App logs : ${CYAN}docker compose logs -f backend${NC}"
    echo -e "  ngrok UI : ${CYAN}http://localhost:4040${NC}"
    echo -e "  Health   : ${CYAN}http://localhost:8080/actuator/health${NC}"
    echo ""
}

main
