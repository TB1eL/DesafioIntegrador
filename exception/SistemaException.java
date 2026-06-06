package exception;

/**
 * Excecao unica do sistema.
 * Usa metodos de fabrica estaticos para substituir as tres classes anteriores:
 *   - AppException       -> SistemaException.deBanco(...)
 *   - ValidacaoException -> SistemaException.deValidacao(...)
 *   - EstoqueInsuficienteException -> SistemaException.deEstoque(...)
 */
public class SistemaException extends RuntimeException {

    private SistemaException(String mensagem) {
        super(mensagem);
    }

    private SistemaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    // Erro de banco / infra (equivale ao AppException antigo)
    public static SistemaException deBanco(String mensagem, Throwable causa) {
        return new SistemaException(mensagem, causa);
    }

    // Erro de validacao de entrada (equivale ao ValidacaoException antigo)
    public static SistemaException deValidacao(String mensagem) {
        return new SistemaException(mensagem);
    }

    // Erro de estoque insuficiente (equivale ao EstoqueInsuficienteException antigo)
    public static SistemaException deEstoque(String produto, int disponivel, int solicitado) {
        return new SistemaException(String.format(
            "Estoque insuficiente para '%s'. Disponivel: %d | Solicitado: %d",
            produto, disponivel, solicitado
        ));
    }
}