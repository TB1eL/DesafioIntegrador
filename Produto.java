package model;

import java.math.BigDecimal;

public class Produto {
    private final int id;
    private final String nome;
    private final BigDecimal preco;
    private final int estoque;
    private final CategoriaProduto categoria;

    public Produto(int id, String nome, BigDecimal preco, int estoque, CategoriaProduto categoria) {
        this.id        = id;
        this.nome      = nome;
        this.preco     = preco;
        this.estoque   = estoque;
        this.categoria = categoria;
    }

    public int getId()                    { return id; }
    public String getNome()               { return nome; }
    public BigDecimal getPreco()          { return preco; }
    public int getEstoque()               { return estoque; }
    public CategoriaProduto getCategoria(){ return categoria; }

    @Override
    public String toString() {
        return String.format("[%d] %-35s | R$ %8.2f | Estoque: %3d | %s",
            id, nome, preco, estoque, categoria);
    }
}
