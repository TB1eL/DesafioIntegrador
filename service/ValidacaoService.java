package service;

import exception.SistemaException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ValidacaoService {

    private static final Pattern REGEX_EMAIL =
        Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.]{2,}$");

    private ValidacaoService() {}

    public static void validarNome(String nome) {
        if (nome == null || nome.isBlank())
            throw SistemaException.deValidacao("O nome nao pode ser vazio.");
        if (nome.length() > 100)
            throw SistemaException.deValidacao("O nome nao pode ter mais de 100 caracteres.");
    }

    public static void validarEmail(String email) {
        if (email == null || email.isBlank())
            throw SistemaException.deValidacao("O e-mail nao pode ser vazio.");
        if (!REGEX_EMAIL.matcher(email).matches())
            throw SistemaException.deValidacao("Formato de e-mail invalido: " + email);
    }

    public static void validarPreco(BigDecimal preco) {
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0)
            throw SistemaException.deValidacao("O preco deve ser maior que zero.");
    }

    public static void validarEstoque(int estoque) {
        if (estoque < 0)
            throw SistemaException.deValidacao("O estoque nao pode ser negativo.");
    }

    public static void validarQuantidade(int qtd) {
        if (qtd <= 0)
            throw SistemaException.deValidacao("A quantidade deve ser maior que zero.");
    }
}