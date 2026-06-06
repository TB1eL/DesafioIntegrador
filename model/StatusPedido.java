package model;

public enum StatusPedido {
    ABERTO,
    FILA,
    PROCESSANDO,
    FINALIZADO;

    public static StatusPedido fromString(String valor) {
        for (StatusPedido s : values()) {
            if (s.name().equalsIgnoreCase(valor)) return s;
        }
        throw new IllegalArgumentException("Status inválido: " + valor);
    }
}
