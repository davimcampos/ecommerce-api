# E-commerce API

API REST de e-commerce com Java 21, Spring Boot 3, Spring Security + JWT, JPA/Hibernate, PostgreSQL, Bean Validation, Swagger e Docker.

## Executando

Com Docker (API + PostgreSQL):

    docker compose up --build

Localmente (requer PostgreSQL em `localhost:5432`, banco/usuário/senha `ecommerce`):

    mvn spring-boot:run

Testes unitários:

    mvn test

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- Administrador criado na inicialização: `admin@ecommerce.com` / `Admin@12345` (variáveis `ADMIN_EMAIL` e `ADMIN_PASSWORD`).
- Troque `JWT_SECRET` (Base64, mínimo 256 bits) fora de ambiente de desenvolvimento: `openssl rand -base64 32`.

## Endpoints

| Módulo | Método e rota | Acesso |
|---|---|---|
| Usuário | `POST /api/auth/register` | público |
| | `POST /api/auth/login` | público |
| | `GET /api/users/me` | autenticado |
| Endereço | `GET/POST /api/users/me/addresses` | autenticado |
| | `PUT/DELETE /api/users/me/addresses/{id}` | autenticado |
| Categorias | `GET /api/categories`, `GET /api/categories/{id}` | público |
| | `POST /api/categories`, `PUT/DELETE /api/categories/{id}` | ADMIN |
| Produtos | `GET /api/products` (filtros: `name`, `categoryId`, `minPrice`, `maxPrice`, `inStockOnly`, `page`, `size`, `sort`) | público |
| | `GET /api/products/{id}` | público |
| | `POST /api/products`, `PUT /api/products/{id}`, `DELETE /api/products/{id}` | ADMIN |
| Estoque | `PATCH /api/products/{id}/stock` com `{"delta": 10}` ou `{"delta": -3}` | ADMIN |
| Carrinho | `GET /api/cart`, `DELETE /api/cart` | autenticado |
| | `POST /api/cart/items` com `{"productId": 1, "quantity": 2}` | autenticado |
| | `PATCH /api/cart/items/{productId}` com `{"quantity": 3}` | autenticado |
| | `DELETE /api/cart/items/{productId}` | autenticado |
| Pedido | `POST /api/orders` com `{"addressId": 1}` | autenticado |
| | `POST /api/orders/{id}/cancel` | autenticado |
| | `GET /api/orders` (histórico paginado), `GET /api/orders/{id}` | autenticado |

## Regras de negócio

- O pedido é criado a partir do carrinho; o estoque é baixado de forma atômica (`UPDATE ... WHERE stock >= qty`), sem condição de corrida. Se qualquer item não tiver estoque, nada é gravado.
- O pedido guarda um snapshot do endereço, nome e preço dos itens no momento da compra.
- Cancelamento só é permitido nos status `PENDING` e `CONFIRMED` e devolve os itens ao estoque.
- Produtos são desativados (exclusão lógica) para preservar o histórico de pedidos.
- Categorias com produtos não podem ser removidas.

## Erros

Todas as respostas de erro seguem o formato:

    {
      "timestamp": "2026-10-07T12:00:00Z",
      "status": 400,
      "error": "Bad Request",
      "message": "Dados inválidos",
      "path": "/api/auth/register",
      "details": { "email": "E-mail inválido" }
    }

Códigos usados: 400 validação, 401 não autenticado/credenciais inválidas, 403 sem permissão, 404 não encontrado, 409 conflito, 422 regra de negócio.
