package model;

public class Cliente {
    private final int id;
    private final String nome;
    private final String email;

    public Cliente(int id, String nome, String email) {
        this.id    = id;
        this.nome  = nome;
        this.email = email;
    }

    public int getId()       { return id; }
    public String getNome()  { return nome; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return String.format("[%d] %-25s | %s", id, nome, email);
    }
}
