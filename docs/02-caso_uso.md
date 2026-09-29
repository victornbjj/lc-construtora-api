# Caso de uso - API de Gerenciamento de Imoveis 

## Atores

 -  **Admin** : Responsavel por operar o sistema pela construtora (responsável por operar o sistema pela construtora. Faz login, cadastra, edita e exclui imóveis, visualiza os contatos recebidos e cadastra novos administradores.)

 - **Visitante** : Pessoa sem login que acessa o site da construtora. Consulta imoveis disponíveis e envia um contato demonstrando interesse.   




 ## Diagrama de Casos de Uso (visão textual)

 ```mermaid
graph TD
    A[VISITANTE] --> UC1[Listar imóveis com paginação e ordenação]
    A --> UC2[Consultar imóvel por ID e buscar por título]
    A --> UC3[Enviar contato]
    
    B[ADMIN]  --> UC4[Fazer login]
    B --> UC5[Cadastrar imóvel]
    B --> UC6[Atualizar imóvel]
    B --> UC7[Excluir imóvel]
    B --> UC8[Listar contatos filtro por imóvel ou data]
    B --> UC9[Consultar contato por ID]
    B --> UC10[Cadastrar novo administrador]
```
> **Regra comum a UC05 a UC10:** são rotas `/admin/**` e exigem login (UC04). Sem token válido, o sistema retorna `401 Unauthorized`; sem permissão, `403 Forbidden` (RF06).
--- 
## UC01 — Listar imóveis
 
**Ator:** Visitante | **Requisito:** RF02
 
**Pré-condição:** nenhuma.
 
**Fluxo principal:**
1. O visitante solicita a lista de imóveis, informando opcionalmente página, tamanho da página e ordenação (preço ou data de cadastro).
2. O sistema busca os imóveis aplicando paginação e ordenação.
3. O sistema retorna `200 OK` com a página de imóveis e o total de itens e de páginas.
**Fluxo alternativo:**
- Nenhum imóvel cadastrado → `200 OK` com lista vazia.
- Campo de ordenação inválido → `400 Bad Request`.
## UC02 — Consultar imóvel
 
**Ator:** Visitante | **Requisito:** RF03
 
**Pré-condição:** nenhuma.
 
**Fluxo principal:**
1. O visitante informa o ID do imóvel ou um termo para buscar por título.
2. O sistema retorna `200 OK` com os dados do imóvel (busca por ID) ou com a lista de imóveis cujo título contém o termo (busca por título).
**Fluxo alternativo:**
- ID inexistente → `404 Not Found`.
- Busca por título sem resultados → `200 OK` com lista vazia.
## UC03 — Enviar contato
 
**Ator:** Visitante | **Requisitos:** RF04, RN01, RNF13
 
**Pré-condição:** nenhuma (não exige login).
 
**Fluxo principal:**
1. O visitante informa nome, e-mail, telefone e motivo (e, opcionalmente, o imóvel de interesse).
2. O sistema valida os dados (RN01: e-mail e telefone com formato válido).
3. O sistema registra o contato com a data de envio.
4. O sistema retorna `201 Created`.
**Fluxo alternativo:**
- Campo obrigatório ausente, ou e-mail/telefone com formato inválido → `400 Bad Request` com a mensagem de erro.
- Imóvel informado não existe → `404 Not Found`.
## UC04 — Fazer login
 
**Ator:** Admin | **Requisitos:** RF05, RN09, RNF09
 
**Pré-condição:** admin cadastrado e ativo.
 
**Fluxo principal:**
1. O admin informa e-mail e senha.
2. O sistema valida as credenciais.
3. O sistema gera um token JWT com tempo de expiração definido.
4. O sistema retorna `200 OK` com o token.
**Fluxo alternativo:**
- E-mail inexistente, senha incorreta ou admin desativado → `401 Unauthorized` com a mensagem "credenciais inválidas", sem revelar se o e-mail existe (RN09).
- Campos vazios → `400 Bad Request`.
## UC05 — Cadastrar imóvel
 
**Ator:** Admin | **Requisitos:** RF01, RF07, RN02, RN03, RN04
 
**Pré-condição:** admin autenticado (UC04).
 
**Fluxo principal:**
1. O admin informa título, descrição, tipo, preço, endereço (cidade, bairro e UF), área, quartos, banheiros, vagas e situação.
2. O sistema valida os dados.
3. O sistema salva o imóvel com a data de cadastro.
4. O sistema retorna `201 Created` com o imóvel criado.
**Fluxo alternativo:**
- Preço ou área menor ou igual a zero (RN02) → `400 Bad Request`.
- Tipo `TERRENO` com quartos, banheiros ou vagas preenchidos (RN03) → `400 Bad Request`.
- Campo obrigatório ausente → `400 Bad Request`.
- Sem autenticação → `401`; sem permissão → `403`.
## UC06 — Atualizar imóvel
 
**Ator:** Admin | **Requisitos:** RF07, RN02, RN03, RN04
 
**Pré-condição:** admin autenticado (UC04) e imóvel existente.
 
**Fluxo principal:**
1. O admin informa o ID do imóvel e os novos dados.
2. O sistema localiza o imóvel.
3. O sistema valida os dados (RN02 e RN03).
4. O sistema atualiza o imóvel.
5. O sistema retorna `200 OK` com o imóvel atualizado.
**Fluxo alternativo:**
- ID inexistente → `404 Not Found`.
- Dados inválidos (preço ou área menor ou igual a zero, terreno com quartos, banheiros ou vagas) → `400 Bad Request`.
- Sem autenticação → `401`; sem permissão → `403`.
## UC07 — Excluir imóvel
 
**Ator:** Admin | **Requisitos:** RF07, RN04, RN08
 
**Pré-condição:** admin autenticado (UC04) e imóvel existente.
 
**Fluxo principal:**
1. O admin informa o ID do imóvel.
2. O sistema localiza o imóvel.
3. O sistema mantém os contatos vinculados, definindo o imóvel deles como nulo (RN08).
4. O sistema exclui o imóvel.
5. O sistema retorna `204 No Content`.
**Fluxo alternativo:**
- ID inexistente → `404 Not Found`.
- Sem autenticação → `401`; sem permissão → `403`.
## UC08 — Listar contatos
 
**Ator:** Admin | **Requisitos:** RF08, RN04, RNF13
 
**Pré-condição:** admin autenticado (UC04).
 
**Fluxo principal:**
1. O admin solicita a lista de contatos, informando opcionalmente página, tamanho da página e filtros (imóvel ou data).
2. O sistema busca os contatos aplicando filtros e paginação.
3. O sistema retorna `200 OK` com a página de contatos.
**Fluxo alternativo:**
- Nenhum contato encontrado → `200 OK` com lista vazia.
- Filtro inválido (ex.: data em formato incorreto) → `400 Bad Request`.
- Sem autenticação → `401`; sem permissão → `403`.
## UC09 — Consultar contato por ID
 
**Ator:** Admin | **Requisitos:** RF08, RN04, RNF13
 
**Pré-condição:** admin autenticado (UC04).
 
**Fluxo principal:**
1. O admin informa o ID do contato.
2. O sistema localiza o contato.
3. O sistema retorna `200 OK` com nome, e-mail, telefone, motivo, data de envio e imóvel vinculado (se houver).
**Fluxo alternativo:**
- ID inexistente → `404 Not Found`.
- Sem autenticação → `401`; sem permissão → `403`.
## UC10 — Cadastrar novo administrador
 
**Ator:** Admin | **Requisitos:** RF09, RF11, RN04, RN05, RN06
 
**Pré-condição:** admin autenticado (UC04).
 
**Fluxo principal:**
1. O admin informa nome, e-mail e senha do novo administrador.
2. O sistema valida os dados (RN06: senha com no mínimo 8 caracteres).
3. O sistema verifica se o e-mail já está em uso (RN05).
4. O sistema salva o usuário com perfil `ADMIN`, ativo, e a senha com hash (BCrypt).
5. O sistema retorna `201 Created` com os dados do novo admin, sem a senha.
**Fluxo alternativo:**
- E-mail já cadastrado → `409 Conflict` (RN05).
- Senha com menos de 8 caracteres ou e-mail em formato inválido → `400 Bad Request`.
- Sem autenticação → `401`; sem permissão → `403`.
> **Observação (RF11):** não existe endpoint público de cadastro. O primeiro admin é criado por migration/seed (RF10).