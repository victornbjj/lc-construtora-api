# SISTEMA DE GESTÃO DE IMOVEIS - LC CONSTRUTORA 


## VISÃO GERAL 
Sistema RESTfulL para gerenciamento de imoveis (Casas, Apartamentos e terrenos)  operações CRUD (Criar, Ler, Atualizar, Deletar) de 
uma construtora de imoveis.

## 2. Requisitos Funcionais (RF)

| ID | Descrição | Prioridade | Status |
|----| ----------| ---------- | ------ |    
|RF01  | O sistema deve permitir cadastrar imóveis com: título, descrição, tipo (casa, apartamento ou terreno), preço, endereço (cidade, bairro, UF), área em m², quartos, banheiros e vagas (quando aplicável), e situação (disponível/vendido/alugado). | OBRIGATORIO | NÃO IMPLEMENTADA| 
|RF02  | O sistema deve listar os imóveis cadastrados com paginação e ordenação (ex: preço, data de cadastro). | OBRIGATORIO | NÃO IMPLEMENTADA|
|RF03  |O sistema deve permitir consultar imóvel por ID (detalhe) e buscar/filtrar por nome/título.  | OBRIGATORIO | NÃO IMPLEMENTADA|
|RF04  |O usuário comum (sem login) deve poder enviar um contato informando nome, e-mail, telefone e motivo .| OBRIGATORIO | NÃO IMPLEMENTADA|
|RF05  |O sistema deve permitir login de administradores (e-mail e senha), retornando um token de acesso. | OBRIGATORIO | NÃO IMPLEMENTADA|
|RF06  |As rotas administrativas (/admin/**) devem exigir autenticação e retornar 401 (não autenticado) ou 403 (sem permissão). | OBRIGATORIO | NÃO IMPLEMENTADA|
|RF07 |O administrador autenticado deve poder criar, atualizar e excluir imóveis. | OBRIGATORIO| NÃO IMPLEMENTADA
| RF08 |O administrador autenticado deve poder consultar os contatos recebidos (listagem paginada e consulta por ID), com filtro por imóvel ou data.| OBRIGATORIO | NÃO IMPLEMENTADA
| RF09 |O administrador autenticado deve poder cadastrar novos administradores| OBRIGATORIO | NÃO IMPLEMENTADA
| RF10 |O primeiro administrador deve ser criado via migration/seed | OBRIGATORIO | NÃO IMPLEMENTADA
| RF11 |Não deve existir endpoint público de cadastro de usuários.| OBRIGATORIO | NÃO IMPLEMENTADA
| RF12 |A API deve ser documentada com Swagger/OpenAPI.| OBRIGATORIO | NÃO IMPLEMENTADA


## 3. Requisitos Não Funcionais (RNF)

| ID | Descrição | Status |
|----|-----------|--------|
| RNF01 | Java 8 com Spring Boot 2.7 | NÃO IMPLEMENTADO |
| RNF02 | Persistência via Spring Data JPA/Hibernate | NÃO IMPLEMENTADO |
| RNF03 | Banco relacional: MySQL (produção/dev) ou H2 (testes) | NÃO IMPLEMENTADO|
| RNF04 | Versionamento de schema via Flyway em `resources/db/migration` | NÃO IMPLEMENTADO |
| RNF05 | Ambiente conteinerizado via Docker/Docker Compose (app + MySQL) | NÃO IMPLEMENTADO |
| RNF06 |Convenções REST: verbos HTTP e códigos de status corretos| NÃO IMPLEMENTADO |
| RNF07 |Tratamento de erros padronizado (`GlobalExceptionHandler`)| NÃO IMPLEMENTADO |
| RNF08 |Código organizado em camadas: Controller, Service, Repository, DTO, Entity | NÃO IMPLEMENTADO |
| RNF09 |Autenticação stateless via JWT com tempo de expiração definido. | NÃO IMPLEMENTADO |
| RNF10 |Credenciais do admin inicial e o segredo do JWT devem vir de variáveis de ambiente, não do código.| NÃO IMPLEMENTADO |
| RNF11 |Testes automatizados nos principais fluxos (login, acesso negado, CRUD de imóveis, envio de contato). | NÃO IMPLEMENTADO |
| RNF12 |README com instruções de execução e, se possível, Docker/docker-compose.| NÃO IMPLEMENTADO |
| RNF13 |Dados pessoais dos contatos tratados conforme a LGPD (coleta mínima e acesso restrito ao admin).| NÃO IMPLEMENTADO |







## Regras de negócio (RN) 

| ID | Descrição |
|----|-----------|
| RN01 |  E-mail e telefone do contato devem ter formato válido. |
| RN02 | Preço e área devem ser maiores que zero.|
| RN03 | Terreno não possui quartos, banheiros nem vagas.|
| RN04 | Apenas administradores autenticados criam, editam ou excluem imóveis, veem contatos e criam usuários.|
| RN05 | E-mail de usuário deve ser único (409 em caso de duplicidade).|
| RN06 | Senha com tamanho mínimo (ex.: 8 caracteres).|
| RN07 | Um administrador não pode excluir ou desativar a si mesmo, nem o último administrador ativo.|
| RN08 | Ao excluir um imóvel, definir o que acontece com os contatos vinculados (manter o histórico com imovelId nulo ou bloquear a exclusão).|
| RN09 | Mensagens de erro de login não devem revelar se o e-mail existe ("credenciais inválidas").|


