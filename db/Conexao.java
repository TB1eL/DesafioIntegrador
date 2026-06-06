package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    // characterEncoding=UTF-8 resolve o problema de acentuação no Windows
    private static final String URL  =
        "jdbc:mysql://localhost:3306/gestao_pedidos" +
        "?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&useUnicode=true";
    private static final String USER = "root";
    private static final String PASS = "";

    private Conexao() {}

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}