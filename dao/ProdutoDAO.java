package dao;

import db.Conexao;
import model.CategoriaProduto;
import model.Produto;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProdutoDAO {

    public void inserir(String nome, BigDecimal preco, int estoque, CategoriaProduto categoria) throws SQLException {
        String sql = "INSERT INTO produtos (nome, preco, estoque, categoria) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setBigDecimal(2, preco);
            ps.setInt(3, estoque);
            ps.setString(4, categoria.name());
            ps.executeUpdate();
        }
    }

    public List<Produto> listarTodos() throws SQLException {
        String sql = "SELECT id, nome, preco, estoque, categoria FROM produtos ORDER BY categoria, nome";
        List<Produto> lista = new ArrayList<>();
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Produto> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, preco, estoque, categoria FROM produtos WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public boolean atualizar(int id, String nome, BigDecimal preco, int estoque, CategoriaProduto categoria) throws SQLException {
        String sql = "UPDATE produtos SET nome = ?, preco = ?, estoque = ?, categoria = ? WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setBigDecimal(2, preco);
            ps.setInt(3, estoque);
            ps.setString(4, categoria.name());
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM produtos WHERE id = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        return new Produto(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getBigDecimal("preco"),
            rs.getInt("estoque"),
            CategoriaProduto.fromString(rs.getString("categoria"))
        );
    }
}
