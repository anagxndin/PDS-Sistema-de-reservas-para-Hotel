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

## Navegação (navbar)

O menu de navegação fica centralizado em templates/fragments/navbar.html
como um fragmento Thymeleaf (th:fragment="navbar(paginaAtual)"), incluído
nas telas autenticadas via:

`html<nav th:replace="~{fragments/navbar :: navbar('home')}"></nav>`

O parâmetro paginaAtual ('home', 'buscar', 'acomodacoes', 'reservas')
controla qual item do menu aparece destacado. Páginas não autenticadas
(login.html, register.html, error.html) não usam o fragmento.

### Acomodações e disponibilidade por data


/acomodacoes/tipos — página de apresentação fixa dos 4 tipos
(Solteiro, Duplo, Suíte, Família), com foto e descrição de cada um. Serve
como porta de entrada para a listagem filtrada.
/acomodacoes — listagem das acomodações cadastradas no banco, com
filtro por tipo via query param (?tipo=DUPLO).
/acomodacoes/buscar — formulário de busca por check-in/check-out
(e tipo opcional).
/acomodacoes/resultado — resultado da busca, calculado a partir da
sobreposição de datas com reservas ativas (PENDENTE/CONFIRMADA) na
tabela reservas.
/api/acomodacoes/disponibilidade — mesmo cálculo de disponibilidade,
exposto como JSON para consumo via fetch/AJAX.

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
- [x] Menu de navegação reutilizável, com item ativo destacado
- [x] Modelo de dados de Acomodação, com CRUD básico (salvar/desativar)
- [x] Listagem de acomodações com filtro por tipo
- [x] Página de apresentação dos tipos de acomodação (com fotos)
- [x] Busca de disponibilidade por período de datas
- [x] Endpoint JSON de disponibilidade para uso via AJAX
- [x] Dados de teste automáticos via data.sql

## Próximos passos (grupo)

- Fluxo de reserva (verificar disponibilidade → registrar → alterar disponibilidade)
- ReservaRepository, ReservaService, ReservaController
- Tela "Minhas reservas" (item já reservado no menu, hoje desabilitado)
- Cancelamento (atualizar status → liberar acomodação)
- Tela de confirmação da reserva
- Tela/endpoint de cadastro de acomodações (hoje só via data.sql/SQL manual)
