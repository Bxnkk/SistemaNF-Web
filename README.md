# Sistema NF 2.0 — Sistema de Notas e Faltas (Web)

Sistema acadêmico desenvolvido em **Java 21 e Spring Boot 4.1.2** para consultar e gerenciar notas, faltas, frequência e situação dos alunos. A aplicação possui perfis de **Professor** e **Aluno**, autenticação com Spring Security e auditoria de alterações com uma **cadeia local de hashes SHA-256**.

> A auditoria é inspirada no encadeamento usado em blockchain, mas **não** utiliza rede distribuída ou mecanismos de consenso.

## Tecnologias

- Java 21 e Spring Boot 4.1.2
- Maven (com Maven Wrapper)
- Spring MVC, Spring Security, Thymeleaf
- Spring JDBC (`JdbcTemplate`), MySQL e MySQL Connector/J
- HTML, CSS e JUnit 5
- PBKDF2 com HMAC-SHA-256 e *salt* para proteção das senhas

## Funcionalidades

### Professor

- Autenticar-se e visualizar alunos das turmas vinculadas ao seu cadastro.
- Pesquisar alunos por nome ou matrícula/RA.
- Cadastrar ou alterar notas (de 0 a 10, com até duas casas decimais) e faltas (de 0 a 120).
- Consultar notas, faltas, frequência e situação acadêmica.
- Verificar a integridade da cadeia de auditoria de alterações acadêmicas.

### Aluno

- Autenticar-se e consultar os próprios registros de nota, faltas, frequência e situação acadêmica, sem permissão para editá-los.

### Regras acadêmicas

- Nota mínima para aprovação: **6,00**.
- Frequência mínima: **75%**, considerando **120 aulas**; são permitidas até **30 faltas**.
- O sistema informa reprovação por nota, por faltas ou por ambos os motivos, conforme os dados.

## Pré-requisitos

- JDK 21 instalado.
- MySQL 8.x em execução.
- Eclipse IDE (opcional) ou terminal Windows.
- Banco de dados criado utilizando o arquivo SQL público fornecido com o projeto. O arquivo contém apenas a estrutura das tabelas, sem dados cadastrados.

## Configurar o banco de dados

1. No MySQL Workbench, abra o arquivo `sistema_notas_faltas_web_publico.sql`, incluído na pasta do projeto, e execute o script em um ambiente de testes.
2. Confira o esquema de destino e importe o script para o servidor MySQL local.
3. Confirme a existência do banco `sistema_notas_faltas_web` e das tabelas usadas pela aplicação.
4. Crie ou utilize um usuário de banco com permissões suficientes para consultar e atualizar dados acadêmicos e os registros de auditoria. O registro da auditoria usa `SELECT ... FOR UPDATE`, portanto as permissões precisam atender a essa operação.
5. Defina as variáveis de ambiente `MYSQL_APP_USER` e `MYSQL_APP_PASSWORD` com as credenciais desse usuário. **Não inclua a senha no código, nos prints ou no README.**

As configurações da conexão ficam em `src/main/resources/application.properties`. A aplicação espera o MySQL em `localhost:3306` e o banco `sistema_notas_faltas_web`.

### Exemplo no PowerShell (Windows)

Abra o PowerShell diretamente na pasta sistemanf-web, onde está localizado o arquivo pom.xml. Configure as variáveis de ambiente e execute os comandos abaixo:

```powershell
$env:MYSQL_APP_USER = "SEU_USUARIO_MYSQL"
$env:MYSQL_APP_PASSWORD = "SUA_SENHA_MYSQL"
.\mvnw.cmd spring-boot:run
```

Se preferir o Eclipse, importe a pasta como **Existing Maven Project**, configure as variáveis de ambiente na configuração de execução e inicie a classe `SistemanfWebApplication` como aplicação Java/Spring Boot.

## Acessar o sistema

Com a aplicação iniciada, abra:

- **Login:** `http://localhost:8080/login`
- **Painel do professor:** `http://localhost:8080/professor` (exige perfil de Professor)
- **Painel do aluno:** `http://localhost:8080/aluno` (exige perfil de Aluno)

O arquivo SQL público contém apenas a estrutura do banco de dados. Para acessar as funcionalidades de Professor e Aluno, é necessário cadastrar usuários de demonstração com senhas protegidas pelo mecanismo PBKDF2 utilizado pela aplicação. As credenciais não são disponibilizadas publicamente.

## Auditoria e integridade

Ao cadastrar ou alterar notas e faltas, o sistema registra informações da alteração e um hash SHA-256 encadeado ao anterior. A tabela `auditoria_academica` armazena registros da auditoria; `auditoria_controle` mantém o último hash conhecido. O professor pode clicar em **Verificar integridade** para recalcular e validar a sequência.

O histórico das operações também é registrado em `historico_alteracoes`. A verificação disponível na interface verifica especificamente a cadeia de `auditoria_academica` e seu controle. O mecanismo serve como evidência de integridade, mas não substitui controles administrativos, backups e segurança do banco.

## Testes automatizados

No diretório raiz do projeto, com o MySQL acessível e as variáveis de ambiente configuradas, execute:

```powershell
.\mvnw.cmd test
```

A suíte inclui testes de validação das notas e faltas, verificação de senhas, verificação da auditoria e carregamento do contexto Spring. Na execução de **22/11/2026**, foram registrados **15 testes executados, 0 falhas, 0 erros e BUILD SUCCESS**.

## Estrutura principal

```text
sistemanf-web/
├── pom.xml
├── mvnw / mvnw.cmd
├── src/
│   ├── main/
│   │   ├── java/br/com/sistemanf/web/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── security/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/css/sistema.css
│   │       └── templates/
│   └── test/java/br/com/sistemanf/web/
└── README.md
```

## Escopo e observações

O Sistema NF 2.0 contempla os perfis de **Professor** e **Aluno**. Um eventual perfil administrativo e comunicados internos ficam como possíveis melhorias futuras, fora do escopo desta versão.

Antes de distribuir o banco de dados, confira se há nomes, matrículas ou outras informações pessoais reais nos registros de exemplo e, se necessário, substitua-os por dados fictícios.
