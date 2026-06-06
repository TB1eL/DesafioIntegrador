package model;

public enum CategoriaProduto {
    NOTEBOOK,
    SMARTPHONE,
    PERIFERICO,
    COMPONENTE,
    ACESSORIO;

    public static CategoriaProduto fromString(String valor) {
        for (CategoriaProduto c : values()) {
            if (c.name().equalsIgnoreCase(valor)) return c;
        }
        throw new IllegalArgumentException("Categoria inválida: " + valor);
    }
}
