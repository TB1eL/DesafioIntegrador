# NiceSystem - NeoEletro
**Sistema de Gestao de Pedidos | Desafio Integrador 3 Periodo - Engenharia de Software**
**Centro Universitario Campo Real**

---

## Ferramentas utilizadas

| Ferramenta            | Funcao                        |
|-----------------------|-------------------------------|
| Java JDK 17+          | Linguagem e compilacao        |
| VS Code               | Editor e execucao             |
| XAMPP                 | Servidor MariaDB local        |
| MySQL Workbench       | Importar o banco e visualizar |
| MySQL Connector/J 9.7 | Driver JDBC (ja incluido)     |

---

## Estrutura do Projeto

```
DesafioIntegrador/
│
├── Main.java
├── banco.sql
│
├── lib/
│   └── mysql-connector-j-9.7.0.jar
│
├── db/
│   └── Conexao.java
│
├── model/
│   ├── Cliente.java
│   ├── Produto.java
│   ├── Pedido.java
│   ├── ItemPedido.java
│   ├── CategoriaProduto.java   (ENUM: NOTEBOOK, SMARTPHONE, PERIFERICO, COMPONENTE, ACESSORIO)
│   └── StatusPedido.java       (ENUM: ABERTO, FILA, PROCESSANDO, FINALIZADO)
│
├── exception/
│   └── SistemaException.java
│
├── dao/
│   ├── ClienteDAO.java
│   ├── ProdutoDAO.java
│   ├── PedidoDAO.java
│   └── ItemPedidoDTO.java
│
├── service/
│   ├── ProcessadorPedidos.java
│   └── ValidacaoService.java
│
└── ui/
    └── Menu.java
```

---

## Passo 1 - Iniciar o XAMPP

1. Abra o **XAMPP Control Panel**
2. Clique em **Start** na linha do **MySQL**
3. Confirme que o status ficou verde (porta 3306)

---

## Passo 2 - Importar o banco no MySQL Workbench

1. Abra o **MySQL Workbench**
2. Conecte na instancia local (`root` sem senha, porta `3306`)
3. No menu superior va em **Server > Data Import**
4. Selecione **Import from Self-Contained File**
5. Clique nos tres pontinhos `...` e selecione o arquivo `banco.sql`
6. Clique em **Start Import**

Isso cria o banco `gestao_pedidos` com todas as tabelas e insere
os dados iniciais (15 clientes e 25 produtos).

> **Alternativa mais rapida:** abra uma aba de query no Workbench,
> cole o conteudo do `banco.sql` e pressione `Ctrl+Shift+Enter`
> para executar tudo de uma vez.

---

## Passo 3 - Configurar o VS Code

### 3.1 Extensoes necessarias

Instale as extensoes abaixo pelo Marketplace do VS Code (`Ctrl+Shift+X`):

- **Extension Pack for Java** (Microsoft) — compilador, debugger e suporte a projetos Java
- **Language Support for Java** (Red Hat) — ja vem junto com o pacote acima

### 3.2 Abrir o projeto

1. No VS Code, va em **File > Open Folder**
2. Selecione a pasta `DesafioIntegrador`

### 3.3 Adicionar o driver JDBC ao classpath

O VS Code precisa saber que o JAR do driver existe para compilar corretamente.

**Opcao A — pelo painel Java Projects (recomendada):**
1. No painel lateral esquerdo clique em **Java Projects** (icone de xicara)
2. Expanda o projeto, clique nos tres pontinhos ao lado de **Referenced Libraries**
3. Selecione o arquivo `lib/mysql-connector-j-9.7.0.jar`

**Opcao B — criando o arquivo `.classpath` manualmente:**

Crie o arquivo `.classpath` na raiz do projeto com o seguinte conteudo:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<classpath>
    <classpathentry kind="src" path=""/>
    <classpathentry kind="lib" path="lib/mysql-connector-j-9.7.0.jar"/>
    <classpathentry kind="output" path="out"/>
</classpath>
```

---

## Passo 4 - Executar

### Pelo VS Code (recomendado)

1. Abra o arquivo `Main.java`
2. Clique no botao **Run** que aparece acima do metodo `main`
   (ou pressione `F5`)

O terminal integrado do VS Code abrira automaticamente com o sistema rodando.

### Pelo terminal (CMD dentro do VS Code)

Abra o terminal com `Ctrl+` ` e execute:

**Compilar:**
```cmd
javac -cp "lib\mysql-connector-j-9.7.0.jar" -d out Main.java db\Conexao.java model\*.java exception\*.java dao\*.java service\*.java ui\*.java
```

**Executar:**
```cmd
java -cp "out;lib\mysql-connector-j-9.7.0.jar" Main
```

---

## Ajustar a conexao (se necessario)

Se o seu MariaDB/MySQL usar senha ou porta diferente, edite `db/Conexao.java`:

```java
private static final String URL  =
    "jdbc:mysql://localhost:3306/gestao_pedidos" +
    "?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true";
private static final String USER = "root";
private static final String PASS = "";   // <- adicione sua senha aqui se tiver
```

---

## Decisoes Arquiteturais

### Isolamento SQL x Console
Nenhuma classe do pacote `ui` importa `java.sql`. Todo acesso ao banco
fica nos DAOs. Erros de banco sao encapsulados em `SistemaException`
(RuntimeException) antes de chegar ao menu, mantendo o isolamento exigido.

### Thread e Gerenciamento de Conexoes
`ProcessadorPedidos` roda como daemon thread. A cada ciclo abre e fecha
sua propria `Connection` via `Conexao.conectar()`, isolada da conexao
do menu principal. Usa UPDATE condicional (`WHERE status = 'FILA'`)
para garantir atomicidade — compativel com MariaDB do XAMPP.

### Sem Setters nos Modelos (Object Calisthenics)
Os objetos `Cliente`, `Produto`, `Pedido` e `ItemPedido` nascem validos
e imutaveis pelo construtor. O `ResultSet` popula via
`new Objeto(rs.getX(...), ...)`. Nenhum setter existe.

### Controle Transacional no Pedido
`PedidoDAO.criarPedido()` usa `setAutoCommit(false)`:
1. Verifica estoque com `SELECT ... FOR UPDATE`
2. Abate com `UPDATE ... WHERE estoque >= qtd`
3. Insere pedido e itens em batch
4. `commit()` — ou `rollback()` completo em qualquer falha

### SistemaException com Factory Methods
Uma unica classe de excecao com metodos estaticos de fabrica:

```java
SistemaException.deBanco("msg", causa)           // erros de JDBC
SistemaException.deValidacao("msg")              // campos invalidos
SistemaException.deEstoque(produto, disp, sol)   // estoque insuficiente
```

---

## Relatorios Gerenciais

| #  | Relatorio                | Tecnicas SQL                                  |
|----|--------------------------|-----------------------------------------------|
| 1  | Vendas por Categoria     | `GROUP BY`, `SUM`, `COUNT`, `AVG`, `ORDER BY` |
| 2  | Ranking de Clientes      | `JOIN` multiplo, `SUM`, `MAX`, `GROUP BY`     |
| 3  | Produtos Estoque Critico | `WHERE estoque < 5`, `ORDER BY ASC`           |