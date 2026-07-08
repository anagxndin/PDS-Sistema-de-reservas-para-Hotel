# Hospedagem App

Sistema web de gerenciamento de reservas de hospedagem (projeto de faculdade).

Este repositório contém apenas a base do projeto: **configuração inicial, cadastro
de usuário e login com JWT**. O restante das funcionalidades (acomodações,
reservas, cancelamento, confirmação) fica por conta do grupo, seguindo a mesma
estrutura de pastas.

## Stack

- Java 21
- Spring Boot 3.3 (Web, Data JPA, Security, Validation, Thymeleaf)
- PostgreSQL 17 (via Docker Compose)
- Maven
- Lombok
- JJWT (JWT)

## Arquitetura

MVC clássico, com os pacotes nomeados exatamente como as camadas do padrão:

```
src/main/java/hospedagem/
├── model/                  # CAMADA MODEL: dados + regra de negocio
│   ├── entity/                 - User.java (entidade JPA)
│   ├── repository/             - UserRepository.java (acesso a dados)
│   ├── service/                - UserService.java, JwtService.java (regra de negocio)
│   └── exception/              - EmailJaCadastradoException.java
│
├── controller/             # CAMADA CONTROLLER: recebe a requisicao, decide o fluxo
│   ├── AuthController.java     - /register, /login
│   ├── HomeController.java     - /home
│   ├── GlobalExceptionHandler.java
│   └── dto/                    - RegisterRequest.java, LoginRequest.java
│
└── config/                 # infraestrutura do Spring Security (nao e uma camada
├── SecurityConfig.java     do MVC, e a "fiacao" que autentica a requisicao
└── JwtAuthFilter.java      antes dela chegar no Controller)

src/main/resources/
├── templates/               # CAMADA VIEW: telas Thymeleaf
├── static/css/              # estilos
└── application.yml
```

`RegisterRequest`/`LoginRequest` ficam em `controller/dto/` (não são
Controllers, são só o formato dos dados que o formulário HTML envia — o
Controller recebe isso como parâmetro via `@ModelAttribute`).

Quando o grupo for adicionar **Acomodação** e **Reserva**, é só replicar o
padrão: `model/entity/Acomodacao.java`, `model/repository/AcomodacaoRepository.java`,
`model/service/AcomodacaoService.java`, e o endpoint em `controller/AcomodacaoController.java`.

## Como rodar

Pré-requisitos: Java 21, Maven 3.9+ e Docker instalados.

### No ubuntu:

```bash
sudo apt update
sudo apt install maven
```

```bash
# sobe o banco e a aplicação
make run
```

A aplicação fica disponível em `http://localhost:8080` (redireciona para `/login`).

Outros comandos úteis:

```bash
make db-up      # sobe só o banco (Docker Compose)
make db-down    # derruba o banco, mantém os dados
make db-reset   # derruba o banco e apaga os dados (recomeça do zero)
make db-logs    # logs do banco
make build      # gera o .jar (mvn clean package)
make test       # roda os testes
make help       # lista todos os comandos
```

## Como funciona a autenticação

- Login é feito em `/login` (formulário Thymeleaf, não é REST).
- Ao autenticar com sucesso, a aplicação gera um **JWT** e guarda em um
  **cookie httpOnly** chamado `jwt`.
- A cada requisição, o `JwtAuthFilter` lê esse cookie, valida o token e
  autentica o usuário no contexto do Spring Security — é isso que faz o JWT
  já estar "no contexto das requisições" sem precisar reenviar credenciais.
- Rotas protegidas (tudo exceto `/`, `/login`, `/register` e assets estáticos)
  exigem esse cookie válido; sem ele, o usuário é redirecionado para `/login`.
- `/logout` limpa o cookie.

Isso é o suficiente para o escopo pedido. Se mais pra frente vocês quiserem
expor os dados como API REST pura (JSON) para um frontend separado (SPA/mobile),
dá pra reaproveitar o `JwtService`/`JwtAuthFilter` quase sem mudanças — só trocar
o `Authorization: Bearer <token>` no lugar do cookie.

## Variáveis de ambiente

O `application.yml` já vem com valores padrão para desenvolvimento local. Em
produção, defina via variável de ambiente:

```
JWT_SECRET=<uma chave secreta forte>
```

## Regras já implementadas

- [x] Não permite cadastro com e-mail duplicado
- [x] Valida campos obrigatórios (nome, e-mail, senha, confirmação de senha)
- [x] Valida formato de e-mail e tamanho mínimo de senha (6 caracteres)
- [x] Não permite login com credenciais inválidas
- [x] Senha armazenada com hash (BCrypt), nunca em texto puro
- [x] Sessão via JWT stateless (não usa `HttpSession`)

## Próximos passos (grupo)

- Entidade `Acomodacao` + consulta com filtro por data
- Entidade `Reserva` (com `status`: PENDENTE, CONFIRMADA, CANCELADA)
- Fluxo de reserva (verificar disponibilidade - registrar - alterar disponibilidade)
- Cancelamento (atualizar status - liberar acomodação)
- Tela de confirmação da reserva
