# NiceSystem — NeoEletro
**Sistema de Gestao de Pedidos | Desafio Integrador 3 Periodo — Engenharia de Software**
**Centro Universitario Campo Real**

---

## Pre-requisitos

| Ferramenta        | Versao minima                        |
|-------------------|--------------------------------------|
| Java JDK          | 17+                                  |
| MySQL / MariaDB   | MySQL 8.0+ ou MariaDB (XAMPP serve)  |
| MySQL Connector/J | Ja incluido em `lib/` (v9.7.0)       |

> O JAR do driver esta em `lib/mysql-connector-j-9.7.0.jar`.
> Nao e necessario baixar nada.

---

## Estrutura do Projeto

```
DesafioIntegrador/
│
├── Main.java                        <- Ponto de entrada
│
├── banco.sql                        <- DDL + dados iniciais
│
├── lib/
│   └── mysql-connector-j-9.7.0.jar <- Driver JDBC (incluido)
│
├── db/
│   └── Conexao.java                 <- Conexao com o banco
│
├── model/
│   ├── Cliente.java
│   ├── Produto.java
│   ├── Pedido.java
│   ├── ItemPedido.java
│   ├── CategoriaProduto.java        <- ENUM: NOTEBOOK, SMARTPHONE, PERIFERICO, COMPONENTE, ACESSORIO
│   └── StatusPedido.java            <- ENUM: ABERTO, FILA, PROCESSANDO, FINALIZADO
│
├── exception/
│   └── SistemaException.java        <- Excecao unica com factory methods
│
├── dao/
│   ├── ClienteDAO.java
│   ├── ProdutoDAO.java
│   ├── PedidoDAO.java               <- Transacoes + 3 relatorios gerenciais
│   └── ItemPedidoDTO.java           <- DTO para criacao de pedido
│
├── service/
│   ├── ProcessadorPedidos.java      <- Thread daemon de processamento
│   └── ValidacaoService.java        <- Regras de validacao
│
└── ui/
    └── Menu.java                    <- Todos os menus em uma unica classe
```

---

## 1. Configurar o Banco de Dados

Com o MySQL/MariaDB rodando (XAMPP ou nativo), execute:

```bash
mysql -u root -p < banco.sql
```

Isso cria o banco `gestao_pedidos`, todas as tabelas e insere os dados de exemplo
(15 clientes e 25 produtos).

Se preferir pelo phpMyAdmin ou MySQL Workbench, importe o arquivo `banco.sql` diretamente.

---

## 2. Ajustar a Conexao (se necessario)

Abra `db/Conexao.java` e altere as credenciais se precisar:

```java
private static final String URL  =
    "jdbc:mysql://localhost:3306/gestao_pedidos" +
    "?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true";
private static final String USER = "root";
private static final String PASS = "";   // <- coloque sua senha aqui se tiver
```

---

## 3. Compilar

Execute o comando abaixo a partir da pasta raiz do projeto (`DesafioIntegrador/`).

**Windows (CMD):**
```cmd
javac -cp "lib\mysql-connector-j-9.7.0.jar" -d out ^
      Main.java db\Conexao.java ^
      model\*.java exception\*.java ^
      dao\*.java service\*.java ui\*.java
```

**Linux / macOS:**
```bash
javac -cp "lib/mysql-connector-j-9.7.0.jar" -d out \
      Main.java db/Conexao.java \
      model/*.java exception/*.java \
      dao/*.java service/*.java ui/*.java
```

> A pasta `out/` sera criada automaticamente com os `.class` compilados.

---

## 4. Executar

**Windows (CMD):**
```cmd
java -cp "out;lib\mysql-connector-j-9.7.0.jar" Main
```

**Linux / macOS:**
```bash
java -cp "out:lib/mysql-connector-j-9.7.0.jar" Main
```

> Atencao: no Windows o separador de classpath e `;`, no Linux/macOS e `:`.

---

## Funcionalidades

| Modulo      | Operacoes disponíveis                                           |
|-------------|----------------------------------------------------------------|
| Clientes    | Listar, Cadastrar, Atualizar, Remover                          |
| Produtos    | Listar, Cadastrar, Atualizar, Remover                          |
| Pedidos     | Listar (com itens e status), Criar (com controle de estoque)   |
| Relatorios  | Vendas por Categoria, Ranking de Clientes, Estoque Critico     |

---

## Decisoes Arquiteturais

### Isolamento SQL x Console
Nenhuma classe do pacote `ui` importa `java.sql`. Todo acesso ao banco fica nos DAOs.
Erros de banco sao capturados e relancados como `SistemaException` (RuntimeException)
antes de chegar a interface, mantendo o isolamento exigido.

### Thread e Gerenciamento de Conexoes
`ProcessadorPedidos` roda como daemon thread. A cada ciclo ela abre e fecha sua
propria `Connection` via `Conexao.conectar()`, completamente isolada da conexao
do menu principal. Usa UPDATE condicional (`WHERE status = 'FILA'`) para garantir
que nenhum pedido seja processado duas vezes — compativel com MariaDB.

### Sem Setters nos Modelos (Object Calisthenics)
Todos os objetos de modelo (`Cliente`, `Produto`, `Pedido`, `ItemPedido`) nascem
validos e imutaveis pelo construtor. O `ResultSet` popula via
`new Objeto(rs.getX(...), ...)`. Nenhum setter existe.

### Controle Transacional no Pedido
`PedidoDAO.criarPedido()` opera com `setAutoCommit(false)`:
1. Verifica estoque com `SELECT ... FOR UPDATE`
2. Abate com `UPDATE ... WHERE estoque >= qtd` (condicional)
3. Insere pedido e itens
4. `commit()` — ou `rollback()` em qualquer falha

### SistemaException com Factory Methods
Uma unica classe substitui as tres anteriores (`AppException`, `ValidacaoException`,
`EstoqueInsuficienteException`) usando o padrao factory method:

```java
SistemaException.deBanco("msg", causa)           // erros de JDBC
SistemaException.deValidacao("msg")              // campos invalidos
SistemaException.deEstoque(produto, disp, sol)   // estoque insuficiente
```

---

## Relatorios Gerenciais

| #  | Relatorio                 | Tecnicas SQL utilizadas                        |
|----|---------------------------|------------------------------------------------|
| 1  | Vendas por Categoria      | `GROUP BY`, `SUM`, `COUNT`, `AVG`, `ORDER BY`  |
| 2  | Ranking de Clientes       | `JOIN` multiplo, `SUM`, `MAX`, `GROUP BY`      |
| 3  | Produtos Estoque Critico  | `WHERE estoque < 5`, `ORDER BY ASC`            |