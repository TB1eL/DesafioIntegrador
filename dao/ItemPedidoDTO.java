package dao;

public class ItemPedidoDTO {
    public final int produtoId;
    public final int quantidade;

    public ItemPedidoDTO(int produtoId, int quantidade) {
        this.produtoId  = produtoId;
        this.quantidade = quantidade;
    }
}
