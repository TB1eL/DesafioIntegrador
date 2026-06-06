package model;

import java.math.BigDecimal;

public class ItemPedido {
    private final int id;
    private final int pedidoId;
    private final int produtoId;
    private final String nomeProduto;
    private final int quantidade;
    private final BigDecimal precoUnitario;

    public ItemPedido(int id, int pedidoId, int produtoId, String nomeProduto,
                      int quantidade, BigDecimal precoUnitario) {
        this.id             = id;
        this.pedidoId       = pedidoId;
        this.produtoId      = produtoId;
        this.nomeProduto    = nomeProduto;
        this.quantidade     = quantidade;
        this.precoUnitario  = precoUnitario;
    }

    public int getId()                   { return id; }
    public int getPedidoId()             { return pedidoId; }
    public int getProdutoId()            { return produtoId; }
    public String getNomeProduto()       { return nomeProduto; }
    public int getQuantidade()           { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public BigDecimal getSubtotal()      { return precoUnitario.multiply(BigDecimal.valueOf(quantidade)); }

    @Override
    public String toString() {
        return String.format("   -> %-35s | Qtd: %2d | Unit: R$ %8.2f | Sub: R$ %8.2f",
            nomeProduto, quantidade, precoUnitario, getSubtotal());
    }
}
