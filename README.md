# 🚧 ForumHub – Arquitetando Respostas em um Mundo de Perguntas

<br/>

##  Sumário

* [📄 Sobre o Projeto](#sobre-o-projeto)
* [🧱 Estrutura de Módulos](#estrutura-de-módulos)
* [☁️ Hospedagem e Ambiente](#hospedagem-e-ambiente)
* [⚙️ Tecnologias Utilizadas](#tecnologias-utilizadas)
* [📦 Instalação e Execução](#instalação-e-execução-local)
* [🛡️ Autenticação e Segurança](#autenticação-e-segurança)
* [🧪 Testes](#testes)
* [📌 Endpoints Principais](#endpoints-principais)
* [🔄 Contribuição](#contribuição)
* [📝 Licença](#licença)
* [📫 Contato](#contato)


## Sobre o Projeto

O **ForumHub** é um projeto SaaS desenvolvido com fins acadêmicos e de aprimoramento pessoal, inspirado em plataformas de perguntas e respostas.
Seu principal objetivo é servir como um laboratório de aprendizado prático sobre arquitetura de sistemas distribuídos, segurança, autenticação moderna
e boas práticas de desenvolvimento web em nuvem.

Com uma estrutura modular composta por três serviços — authorization-server, topic e user — o projeto simula um ambiente real de fórum colaborativo,
permitindo testar conceitos como autenticação baseada em tokens JWT, isolamento de domínios de negócio, integração entre microsserviços e deploy em ambientes cloud.

Embora não tenha sido pensado para uso corporativo, o ForumHub busca refletir os desafios e padrões encontrados em aplicações SaaS modernas,
sendo um espaço ideal para explorar, experimentar e evoluir como desenvolvedor.
Cada módulo é desacoplado e pode ser desenvolvido e escalado de forma independente.

* `authorization-server`: gerenciamento de autenticação e autorização.
* `topic`: controle e manipulação de tópicos.
* `user`: administração e manutenção dos dados de usuários.

Hospedado atualmente em ambiente de nuvem para maior escalabilidade e disponibilidade.


## Estrutura de Módulos

```
forumhub/
├── authorization-server/
├── topic/
├── user/
└── README.md (este arquivo)
```

### 🔐 `authorization-server`

Responsável pelo autenticação, emissão e assinaturas de tokens JWT, refresh tokens, e políticas de acesso com OAuth2 e Spring Security.

### 🗂️ `topic`

Gerencia a criação de tópicos, criação de respostas associadas aos tópicos, e a criação de cursos utilizados para categorizar cada tópico.

### 👤 `user`

Gerencia a criação de usuários, perfis, permissões e dados de usuários.


## Hospedagem e Ambiente

Atualmente, o sistema está hospedado na nuvem, com suporte a ambiente de **produção**.

* Provedora e CI/CD pipeline: [Render](https://render.com/)
* Banco de dados: `PostgreSQL`

### Inicialização dos módulos do ambiente de produção:
Para inicializar os módulos, é necessário enviar uma operação do tipo `GET` para os endpoints abaixo.
Para isso, envie uma requisição para eles no browser na ordem que se segue e aguarde até que estejam `healthly`.
A inicialização de cada módulo pode levar de 3 a 5 minutos.

1. Módulo Authorization Server:
   https://authorization-server-module.onrender.com/actuator/health

2. Módulo Topico:
   https://topic-module.onrender.com/actuator/health

3. Módulo User:
   https://user-module-tf6y.onrender.com/actuator/health

Ao receber o retorno a seguir, significa que o módulo foi inicializado e está pronto para receber e processar as requisições:
```
{
    "status": "UP",
    "groups": [
        "liveness",
        "readiness"
    ]
}
```

Após a inicialização dos módulos, será possível acessar a documentação baseada na especificação OpenAPI de cada módulo - com exceção do Authorization Server, pois este é integrado
nos módulos `topic` e `user` - para conhecer os detalhes de cada endpoint:

1. Especificação do módulo Topico:
   https://topic-module.onrender.com/forumhub.io/api/v1/swagger-ui.html

2. Especificação do módulo User:
   https://user-module-tf6y.onrender.com/forumhub.io/api/v1/swagger-ui/index.html

>Observe que após 60s sem interação em um dos módulos, este ficará inativo, e será necessário
o reenvio da requisição para o endpoint `<base_url>/actuator/health` novamente para o retorno de sua atividade.

> Dois usuários com elevação estão disponíveis para testes: ADM (perfil ADM) e MOD (perfil MOD). 
> 
> <ins>Credenciais do user ADM</ins>:
> * user: adm@email.com
> * password: adm123
>
> <ins>Credenciais do user MOD</ins>:
> * user: mod@email.com
> * password: mod123

## Tecnologias Utilizadas

* Linguagem: `Java`
* Framework: `Spring Framework / Spring Boot / Spring Data / Spring Authorization Server / Spring Session / Spring Hateoas`
* Banco de dados: `PostgreSQL`
* Autenticação e autorização: `Spring Security / OAuth2 / JWT`
* API: `REST`
* Contêineres: `Docker`
* Outros: `FlywayDB / Spring Actuator / JUnit / Mockito / MockWebServer / OpenAPI / Swagger / Passay for password policy`


## Instalação e Execução Local

### Requisitos

* `Docker` e `Docker Compose`
* `Java 17` ou superior

### Instalação

Clone o projeto e acesse o diretório `forumhub`.
```shell
git clone https://github.com/rcmafra/forumhub.git
cd forumhub
```

### Execução do PostresSQL com Docker

Dentro do diretório do projeto, execute a instrução abaixo para a inicialização do banco de dados PostegreSQL em um contêiner do Docker.
Veja que as credenciais da base de dados estão descritos em [.env file](https://github.com/rcmafra/forumhub/blob/main/.env)
```shell
docker-compose --env-file .env up --build
```

### Execução dos módulos

Para cada módulo, deverá ser utilizado um novo shell (Terminal, CMD, Powershell, etc...).
Caso o shell utilizado seja um bash, substituia `.\mvnw` por `./mvnw`, por exemplo:
`./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`

Acesse o diretório do módulo em um shell na ordem em que se segue. Lembre-se de abrir um novo shell para cada módulo.

```shell
cd .../forumhub/authorization-server
```
```shell
cd .../forumhub/topic
```
```shell
cd .../forumhub/user
```

Após acessar o diretório de cada módulo, execute a instrução a seguir em cada um deles para inicializar.
```shell
.\mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Os usuários com elevação são os mesmos descritos em [Inicialização dos módulos do ambiente de produção](#Inicialização-dos-módulos-do-ambiente-de-produção).

## Autenticação e Segurança

* Tokens JWT com expiração e renovação via refresh token.
* Middleware de proteção por escopo/role.
* CORS configurado.
* Proteção contra ataques comuns: SQLi, XSS, Sniffing, CSRF, etc.


## Testes

* Testes unitários e de integração: `JUnit / Mockito / MockWebServer`
* Coverage report: `JaCoCo`

Para a execução dos testes dos módulos `topic` e `user`, execute a instrução abaixo no diretório de cada módulo a partir de um shell.
Veja que é necessário interromper os módulos inicializados anteriormente antes da execução dos testes.
```shell 
.\mvnw verify
```
O relatório do code coverage estará disponível nos caminhos que se segue:
* Caminho do teste de unitário: `.../<base_path_module>/target/site/jacoco/index.html`
* Caminho do teste de integração: `.../<base_path_module>/target/site/jacoco-it/index.html` 

Exemplo:
```shell
cd .../forumhub/topic/target/site/jacoco/index.html
```

## Endpoints Principais

### 🔐 Módulo Authorization Server

| Método | Rota                           | Módulo               | Descrição                                            |
| ------ |--------------------------------| -------------------- |------------------------------------------------------|
| GET    | `<base_url>/oauth2/authorize`  | authorization-server | Obtém um authorization code.                         |
| POST   | `<base_url>/oauth2/token`      | authorization-server | Obtém um access token através do authorizaion code.  |
| POST   | `<base_url>/oauth2/token`      | authorization-server | Obtém um novo access token através do refresh token. |
| POST   | `<base_url>/oauth2/introspect` | authorization-server | Permite visualizar os detalhes do access token.      |
| POST   | `<base_url>/oauth2/revoke`     | authorization-server | Permite revogar um refresh token.                    |


### 🗂️ Módulo Tópico

| Método | Rota                                                                         | Módulo  | Descrição                  |
|--------|------------------------------------------------------------------------------|---------|----------------------------|
| POST   | `<base_url>/forumhub.io/api/v1/topics/create`                                | topic   | Cria um novo tópico.       |
| GET    | `<base_url>/forumhub.io/api/v1/topics?topic_id=<topic_id>`                   | topic   | Obtém um tópico.           |
| GET    | `<base_url>/forumhub.io/api/v1/topics/listAll`                               | topic   | Lista todos os tópicos.    |
| PUT    | `<base_url>/forumhub.io/api/v1/topics/<topic_id>/edit`                       | topic   | Edita um tópico.           |
| DELETE | `<base_url>/forumhub.io/api/v1/topics/<topic_id>/delete`                     | topic   | Remove um tópico.          |
| POST   | `<base_url>/forumhub.io/api/v1/topics/<topic_id>/answer`                     | topic   | Responde um tópico.        |
| PATCH  | `<base_url>/forumhub.io/api/v1/topics/<topic_id>/answers/<answer_id>/edit`   | topic   | Edita uma resposta.        |
| DELETE | `<base_url>/forumhub.io/api/v1/topics/<topic_id>/answers/<answer_id>/delete` | topic   | Remove uma resposta.       |
| POST   | `<base_url>/forumhub.io/api/v1/courses/create`                               | topic   | Cria um curso.             |
| GET    | `<base_url>/forumhub.io/api/v1/courses?course_id=<course_id>`                | topic   | Obtém um curso.            |
| GET    | `<base_url>/forumhub.io/api/v1/courses/listAll`                              | topic   | Lista todos os cursos.     |
| PUT    | `<base_url>/forumhub.io/api/v1/courses/<course_id>/edit`                     | topic   | Edita um curso.            |
| DELETE | `<base_url>/forumhub.io/api/v1/courses/<course_id>/delete`                   | topic   | Remove um curso.           |
| GET    | `<base_url>/forumhub.io/api/v1/categories/listAll`                           | topic   | Lista todas as categorias. |



### 👤 Módulo User

| Método | Rota                                                                    | Módulo   | Descrição                                   |
|--------|-------------------------------------------------------------------------|----------|---------------------------------------------|
| POST   | `<base_url>/forumhub.io/api/v1/users/create`                            | user     | Cria um novo usuário.                       |
| GET    | `<base_url>/forumhub.io/api/v1/users/summary-info?user_id=<user_id>`    | user     | Obtém as informações resumidas do usuário.  |
| GET    | `<base_url>/forumhub.io/api/v1/users/detailed-info?user_id=<user_id>`   | user     | Obtém as informações detalhadas do usuário. |
| GET    | `<base_url>/forumhub.io/api/v1/users/listAll`                           | user     | Lista todos os usuários.                    |
| PUT    | `<base_url>/forumhub.io/api/v1/users/edit`                              | user     | Edita um usuário.                           |
| DELETE | `<base_url>/forumhub.io/api/v1/users/delete/<user_id>`                  | user     | Remove um usuário.                          |


> **Mais detalhes sobre cada endpoint, assim como o payload (se aplicável), pode ser visualizado na documentação baseado na especificação OpenAPI de acordo com o serviço:**
> * <ins>Módulo Authorization Server:</ins> Como esse módulo é integrado nos módulos `topic` e `user`, não há uma documentação com a especificação OpenAPI,
    porém os detalhes de cada enpoint pode ser visualizado em: [The OAuth 2.0 Authorization Framework](https://datatracker.ietf.org/doc/html/rfc6749)
> * <ins>Módulo tópico:</ins> [Topic module - OpenAPI Spec](https://topic-module.onrender.com/forumhub.io/api/v1/swagger-ui.html)
> * <ins>Módulo User:</ins> [User module - OpenAPI Spec](https://user-module-tf6y.onrender.com/forumhub.io/api/v1/swagger-ui/index.html)



## Contribuição

Contribuições são bem-vindas! Para colaborar:

1. Fork este repositório
2. Crie uma branch: `git checkout -b minha-feature`
3. Faça o commit: `git commit -m 'Minha nova feature'`
4. Push na branch: `git push origin minha-feature`
5. Abra um Pull Request


## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).


## Contato

* Autor: [Raul César](https://github.com/rcmafra/forumhub)
* Email: [raulcesar.sm@gmail.com](mailto:raulcesar.sm@gmail.com)
* LinkedIn: [Raul César](https://www.linkedin.com/in/raulcesar/)
