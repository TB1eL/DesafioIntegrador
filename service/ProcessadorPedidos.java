package service;

import dao.PedidoDAO;
import model.StatusPedido;

import java.util.Optional;

/**
 * Roda em background, processa pedidos da fila.
 * Cada ciclo abre e fecha sua propria conexao (via PedidoDAO).
*/
public class ProcessadorPedidos implements Runnable {

    private static final int INTERVALO_MS   = 3000;
    private static final int TEMPO_PROCESSO = 4000;

    private volatile boolean rodando = true;
    private final PedidoDAO pedidoDAO;

    public ProcessadorPedidos() {
        this.pedidoDAO = new PedidoDAO();
    }

    public void parar() {
        rodando = false;
    }

    @Override
    public void run() {
        log("Thread de processamento iniciada.");
        while (rodando) {
            try {
                Optional<Integer> proximo = pedidoDAO.buscarEReservarProximo();
                if (proximo.isPresent()) {
                    int id = proximo.get();
                    log("Processando pedido #" + id + "...");
                    Thread.sleep(TEMPO_PROCESSO);
                    pedidoDAO.atualizarStatus(id, StatusPedido.FINALIZADO);
                    log("Pedido #" + id + " FINALIZADO.");
                } else {
                    Thread.sleep(INTERVALO_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log("Erro: " + e.getMessage());
            }
        }
        log("Thread encerrada.");
    }

    private static synchronized void log(String msg) {
        System.out.println("\n  [PROCESSOR] " + msg);
    }
}
