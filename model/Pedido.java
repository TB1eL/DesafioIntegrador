package model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class Pedido {
    private final int id;
    private final int clienteId;
    private final String nomeCliente;
    private final StatusPedido status;
    private final List<ItemPedido> itens;

    public Pedido(int id, int clienteId, String nomeCliente,
                  StatusPedido status, List<ItemPedido> itens) {
        this.id          = id;
        this.clienteId   = clienteId;
        this.nomeCliente = nomeCliente;
        this.status      = status;
        this.itens       = Collections.unmodifiableList(itens);
    }

    public int getId()                  { return id; }
    public int getClienteId()           { return clienteId; }
    public String getNomeCliente()      { return nomeCliente; }
    public StatusPedido getStatus()     { return status; }
    public List<ItemPedido> getItens()  { return itens; }

    public BigDecimal getTotal() {
        return itens.stream()
            .map(ItemPedido::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Pedido #%d | Cliente: %-20s | Status: %-11s | Total: R$ %.2f",
            id, nomeCliente, status, getTotal()));
        for (ItemPedido item : itens) {
            sb.append("\n").append(item);
        }
        return sb.toString();
    }
}
