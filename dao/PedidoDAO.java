package dao;

import db.Conexao;
import exception.EstoqueInsuficienteException;
import model.ItemPedido;
import model.Pedido;
import model.StatusPedido;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoDAO {

    /**
     * Cria pedido de forma transacional:
     * 1. Verifica estoque de cada item (leitura com FOR UPDATE)
     * 2. Abate estoque via UPDATE condicional (estoque >= qtd)
     * 3. Insere o pedido e seus itens
     * Qualquer falha faz rollback completo.
     */
    public int criarPedido(int clienteId, List<ItemPedidoDTO> itens)
            throws SQLException, EstoqueInsuficienteException {

        String sqlVerifica  = "SELECT nome, estoque FROM produtos WHERE id = ? FOR UPDATE";
        String sqlAbateEst  = "UPDATE produtos SET estoque = estoque - ? " +
                              "WHERE id = ? AND estoque >= ?";
        String sqlPedido    = "INSERT INTO pedidos (cliente_id, status) VALUES (?, 'FILA')";
        String sqlItem      = "INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, preco_unit) " +
                              "VALUES (?, ?, ?, (SELECT preco FROM produtos WHERE id = ?))";

        Connection conn = Conexao.conectar();
        try {
            conn.setAutoCommit(false);

            // --- Verificação e abate de estoque ---
            for (ItemPedidoDTO dto : itens) {
                try (PreparedStatement psVer = conn.prepareStatement(sqlVerifica)) {
                    psVer.setInt(1, dto.produtoId);
                    try (ResultSet rs = psVer.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Produto ID " + dto.produtoId + " não encontrado.");
                        int estoqueAtual = rs.getInt("estoque");
                        String nome      = rs.getString("nome");
                        if (estoqueAtual < dto.quantidade) {
                            conn.rollback();
                            throw new EstoqueInsuficienteException(nome, estoqueAtual, dto.quantidade);
                        }
                    }
                }
                try (PreparedStatement psAbate = conn.prepareStatement(sqlAbateEst)) {
                    psAbate.setInt(1, dto.quantidade);
                    psAbate.setInt(2, dto.produtoId);
                    psAbate.setInt(3, dto.quantidade);
                    int afetadas = psAbate.executeUpdate();
                    if (afetadas == 0) {
                        conn.rollback();
                        throw new SQLException("Falha ao abater estoque do produto ID " + dto.produtoId);
                    }
                }
            }

            // --- Insere o pedido ---
            int pedidoId;
            try (PreparedStatement psPed = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                psPed.setInt(1, clienteId);
                psPed.executeUpdate();
                try (ResultSet rs = psPed.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("Falha ao obter ID do pedido.");
                    pedidoId = rs.getInt(1);
                }
            }

            // --- Insere itens ---
            try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                for (ItemPedidoDTO dto : itens) {
                    psItem.setInt(1, pedidoId);
                    psItem.setInt(2, dto.produtoId);
                    psItem.setInt(3, dto.quantidade);
                    psItem.setInt(4, dto.produtoId);
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            conn.commit();
            return pedidoId;

        } catch (EstoqueInsuficienteException e) {
            throw e;
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public List<Pedido> listarTodos() throws SQLException {
        String sqlPedidos =
            "SELECT p.id, p.cliente_id, c.nome AS nome_cliente, p.status " +
            "FROM pedidos p JOIN clientes c ON c.id = p.cliente_id " +
            "ORDER BY p.id DESC";
        String sqlItens =
            "SELECT ip.id, ip.pedido_id, ip.produto_id, pr.nome AS nome_produto, " +
            "       ip.quantidade, ip.preco_unit " +
            "FROM itens_pedido ip JOIN produtos pr ON pr.id = ip.produto_id " +
            "WHERE ip.pedido_id = ?";

        List<Pedido> pedidos = new ArrayList<>();
        try (Connection conn = Conexao.conectar();
             PreparedStatement psPed = conn.prepareStatement(sqlPedidos);
             ResultSet rsPed = psPed.executeQuery()) {

            while (rsPed.next()) {
                int pedidoId = rsPed.getInt("id");
                List<ItemPedido> itens = new ArrayList<>();

                try (PreparedStatement psItem = conn.prepareStatement(sqlItens)) {
                    psItem.setInt(1, pedidoId);
                    try (ResultSet rsItem = psItem.executeQuery()) {
                        while (rsItem.next()) {
                            itens.add(new ItemPedido(
                                rsItem.getInt("id"),
                                rsItem.getInt("pedido_id"),
                                rsItem.getInt("produto_id"),
                                rsItem.getString("nome_produto"),
                                rsItem.getInt("quantidade"),
                                rsItem.getBigDecimal("preco_unit")
                            ));
                        }
                    }
                }

                pedidos.add(new Pedido(
                    pedidoId,
                    rsPed.getInt("cliente_id"),
                    rsPed.getString("nome_cliente"),
                    StatusPedido.fromString(rsPed.getString("status")),
                    itens
                ));
            }
        }
        return pedidos;
    }

    public Optional<Pedido> buscarPorId(int id) throws SQLException {
        return listarTodos().stream().filter(p -> p.getId() == id).findFirst();
    }

    /**
     * Usado pelo ProcessadorPedidos: tenta reservar atomicamente o próximo
     * pedido em FILA usando UPDATE condicional — compatível com MariaDB.
     *
     * Estratégia: UPDATE direto com WHERE status = 'FILA' ORDER BY id LIMIT 1.
     * Se afetou 1 linha, usamos LAST_INSERT_ID() — não, usamos SELECT para
     * descobrir qual ID acabou de virar PROCESSANDO (o menor id em PROCESSANDO
     * que não tenha sido finalizado ainda e que foi modificado agora).
     *
     * Na prática, como a thread roda sozinha em ciclo único, o padrão
     * busca → UPDATE condicional → confirma por affected rows é suficiente
     * e seguro para o contexto do projeto.
     */
    public Optional<Integer> buscarEReservarProximo() throws SQLException {
        // Passo 1: busca o menor id em FILA (leitura simples, sem lock)
        String sqlBusca  = "SELECT id FROM pedidos WHERE status = 'FILA' ORDER BY id LIMIT 1";
        // Passo 2: tenta marcar como PROCESSANDO apenas se ainda estiver FILA
        //          (UPDATE condicional — garante atomicidade sem SKIP LOCKED)
        String sqlUpdate = "UPDATE pedidos SET status = 'PROCESSANDO' " +
                           "WHERE id = ? AND status = 'FILA'";

        Connection conn = Conexao.conectar();
        try {
            conn.setAutoCommit(false);

            int pedidoId;
            try (PreparedStatement psBusca = conn.prepareStatement(sqlBusca);
                 ResultSet rs = psBusca.executeQuery()) {
                if (!rs.next()) {
                    conn.rollback();
                    return Optional.empty();
                }
                pedidoId = rs.getInt("id");
            }

            try (PreparedStatement psUp = conn.prepareStatement(sqlUpdate)) {
                psUp.setInt(1, pedidoId);
                int afetadas = psUp.executeUpdate();
                if (afetadas == 0) {
                    // Outra thread já pegou este pedido entre o SELECT e o UPDATE
                    conn.rollback();
                    return Optional.empty();
                }
            }

            conn.commit();
            return Optional.of(pedidoId);
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public void atualizarStatus(int pedidoId, StatusPedido status) throws SQLException {
        String sql = "UPDATE pedidos SET status = ? WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, pedidoId);
            ps.executeUpdate();
        }
    }

    // ---------------------------------------------------------------
    // Relatório 1: Resumo de vendas por categoria
    // ---------------------------------------------------------------
    public void relatorioVendasPorCategoria() throws SQLException {
        String sql =
            "SELECT pr.categoria, " +
            "       COUNT(DISTINCT p.id)         AS total_pedidos, " +
            "       SUM(ip.quantidade)            AS unidades_vendidas, " +
            "       SUM(ip.quantidade * ip.preco_unit) AS receita_total, " +
            "       AVG(ip.preco_unit)            AS ticket_medio " +
            "FROM itens_pedido ip " +
            "JOIN produtos pr  ON pr.id  = ip.produto_id " +
            "JOIN pedidos  p   ON p.id   = ip.pedido_id " +
            "WHERE p.status IN ('FILA','PROCESSANDO','FINALIZADO') " +
            "GROUP BY pr.categoria " +
            "ORDER BY receita_total DESC";

        System.out.println("\n  CATEGORIA        | Pedidos | Unidades | Receita (R$)  | Ticket Médio");
        System.out.println("  " + "-".repeat(74));
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.printf("  %-16s |  %5d  |  %6d  | %13.2f | %12.2f%n",
                    rs.getString("categoria"),
                    rs.getInt("total_pedidos"),
                    rs.getInt("unidades_vendidas"),
                    rs.getBigDecimal("receita_total"),
                    rs.getBigDecimal("ticket_medio"));
            }
        }
    }

    // ---------------------------------------------------------------
    // Relatório 2: Ranking de clientes por valor total gasto
    // ---------------------------------------------------------------
    public void relatorioRankingClientes() throws SQLException {
        String sql =
            "SELECT c.nome, " +
            "       COUNT(DISTINCT p.id)                  AS total_pedidos, " +
            "       SUM(ip.quantidade * ip.preco_unit)    AS total_gasto, " +
            "       MAX(ip.quantidade * ip.preco_unit)    AS maior_compra " +
            "FROM clientes c " +
            "JOIN pedidos      p  ON p.cliente_id  = c.id " +
            "JOIN itens_pedido ip ON ip.pedido_id  = p.id " +
            "WHERE p.status IN ('FILA','PROCESSANDO','FINALIZADO') " +
            "GROUP BY c.id, c.nome " +
            "ORDER BY total_gasto DESC";

        System.out.println("\n  CLIENTE                   | Pedidos | Total Gasto   | Maior Compra");
        System.out.println("  " + "-".repeat(67));
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.printf("  %-25s |  %5d  | %13.2f | %12.2f%n",
                    rs.getString("nome"),
                    rs.getInt("total_pedidos"),
                    rs.getBigDecimal("total_gasto"),
                    rs.getBigDecimal("maior_compra"));
            }
        }
    }

    // ---------------------------------------------------------------
    // Relatório 3: Produtos com baixo estoque (< 5 unidades)
    // ---------------------------------------------------------------
    public void relatorioBaixoEstoque() throws SQLException {
        String sql =
            "SELECT id, nome, preco, estoque, categoria " +
            "FROM produtos " +
            "WHERE estoque < 5 " +
            "ORDER BY estoque ASC";

        System.out.println("\n  ID  | PRODUTO                             | Preço (R$) | Estoque | Categoria");
        System.out.println("  " + "-".repeat(78));
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean temRegistros = false;
            while (rs.next()) {
                temRegistros = true;
                System.out.printf("  %3d | %-35s | %10.2f |  %5d  | %s%n",
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getBigDecimal("preco"),
                    rs.getInt("estoque"),
                    rs.getString("categoria"));
            }
            if (!temRegistros) System.out.println("  Nenhum produto com estoque crítico.");
        }
    }
}