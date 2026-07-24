ifeq ($(OS),Windows_NT)
SHELL := C:/Program Files/Git/bin/bash.exe
endif

.PHONY: help db-up db-down db-logs db-reset run build test clean dev

help: ## Lista os comandos disponiveis
	@echo "db-up      - Sobe o banco Postgres via Docker Compose"
	@echo "db-down    - Derruba o banco (mantem os dados no volume)"
	@echo "db-reset   - Derruba o banco e apaga os dados (recomeca do zero)"
	@echo "db-logs    - Mostra os logs do banco"
	@echo "run        - Sobe o banco (se necessario) e roda a aplicacao"
	@echo "build      - Compila e empacota o projeto (gera o .jar)"
	@echo "test       - Roda os testes"
	@echo "clean      - Limpa artefatos de build do Maven"

db-up: ## Sobe o banco Postgres via Docker Compose
	docker compose up -d
	@echo "Aguardando o banco ficar saudavel..."
	@until docker compose ps db | grep -q "healthy"; do sleep 1; done
	@echo "Banco pronto em localhost:5432"

db-down: ## Derruba o banco (mantem os dados no volume)
	docker compose down

db-reset: ## Derruba o banco e apaga os dados (recomeca do zero)
	docker compose down -v

db-logs: ## Mostra os logs do banco
	docker compose logs -f db

run: db-up ## Sobe o banco (se necessario) e roda a aplicacao
	mvn spring-boot:run

build: ## Compila e empacota o projeto (gera o .jar)
	mvn clean package

test: ## Roda os testes
	mvn test

clean: ## Limpa artefatos de build do Maven
	mvn clean
