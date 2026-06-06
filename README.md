# NiceSystem — NeoEletro
**Sistema de Gestão de Pedidos | Desafio Integrador 3° Período — Engenharia de Software**

---

## Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Java JDK | 17+ |
| MySQL Server | 8.0+ |
| MySQL Connector/J | 8.x (JAR) |

---

## 1. Configurar o Banco de Dados

```bash
mysql -u root -p < banco.sql
```

Isso cria o banco `gestao_pedidos`, todas as tabelas e insere os dados de exemplo.

---

## 2. Ajustar a Conexão

Edite `src/db/Conexao.java` se necessário:

```java
private static final String URL  = "jdbc:mysql://localhost:3306/gestao_pedidos?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASS = "";   // <-- sua senha aqui
```

---

## 3. Compilar

Coloque o JAR do MySQL Connector na pasta `lib/`.

```bash
# Linux / macOS
javac -cp "lib/mysql-connector-j-8.x.jar" -d out -sourcepath src \
      src/Main.java src/db/*.java src/model/*.java src/exception/*.java \
      src/dao/*.java src/service/*.java src/ui/*.java

# Windows
javac -cp "lib\mysql-connector-j-8.x.jar" -d out -sourcepath src ^
      src\Main.java src\db\*.java src\model\*.java src\exception\*.java ^
      src\dao\*.java src\service\*.java src\ui\*.java
```

---

## 4. Executar

```bash
# Linux / macOS
java -cp "out:lib/mysql-connector-j-8.x.jar" Main

# Windows
java -cp "out;lib\mysql-connector-j-8.x.jar" Main
```

---

## Estrutura de Pacotes

```
src/
├── Main.java                  ← Ponto de entrada
├── db/
│   └── Conexao.java           ← Utilitário de conexão JDBC
├── model/
│   ├── Cliente.java
│   ├── Produto.java
│   ├── CategoriaProduto.java  ← ENUM de categorias
│   ├── Pedido.java
│   ├── ItemPedido.java
│   └── StatusPedido.java      ← ENUM de status
├── exception/
│   ├── EstoqueInsuficienteException.java
│   └── ValidacaoException.java
├── dao/
│   ├── ClienteDAO.java
│   ├── ProdutoDAO.java
│   ├── PedidoDAO.java         ← Relatórios gerenciais + transações
│   └── ItemPedidoDTO.java
├── service/
│   ├── ProcessadorPedidos.java ← Thread background
│   └── ValidacaoService.java
└── ui/
    ├── MenuCliente.java        ← SEM import java.sql
    ├── MenuProduto.java        ← SEM import java.sql
    ├── MenuPedido.java         ← SEM import java.sql
    └── MenuRelatorio.java      ← SEM import java.sql
```

---

## Decisões Arquiteturais

### Isolamento SQL × Console
Nenhuma classe do pacote `ui` importa `java.sql`. Toda lógica de banco fica nos DAOs. Os menus conhecem apenas modelos e DAOs, respeitando o princípio de responsabilidade única.

### Thread e Conexões
`ProcessadorPedidos` roda como daemon thread. A cada ciclo ela cria e fecha sua **própria** `Connection` via `Conexao.conectar()` — completamente isolada da conexão do menu principal. O mecanismo `SELECT ... FOR UPDATE SKIP LOCKED` garante que duas threads nunca processem o mesmo pedido.

### Sem Setters nos Modelos
Todos os objetos (`Cliente`, `Produto`, `Pedido`, `ItemPedido`) nascem completos e imutáveis pelo construtor, conforme Object Calisthenics. O `ResultSet` popula apenas via `new Objeto(rs.getX(...), ...)`.

### Controle Transacional no Pedido
O método `PedidoDAO.criarPedido()` usa uma única `Connection` com `setAutoCommit(false)`. Verifica estoque com `FOR UPDATE`, abate com UPDATE condicional (`WHERE estoque >= qtd`) e faz rollback completo em caso de falha, garantindo atomicidade.

---

## Relatórios Gerenciais

| # | Relatório | Técnicas SQL |
|---|---|---|
| 1 | Vendas por Categoria | `GROUP BY`, `SUM`, `COUNT`, `AVG`, `ORDER BY` |
| 2 | Ranking de Clientes | `JOIN` múltiplo, `SUM`, `MAX`, `GROUP BY` |
| 3 | Estoque Crítico | `WHERE`, `ORDER BY ASC` |
