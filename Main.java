import exception.SistemaException;
import service.ProcessadorPedidos;
import ui.Menu;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ProcessadorPedidos processador = new ProcessadorPedidos();
        Thread t = new Thread(processador);
        t.setDaemon(true);
        t.start();

        Scanner sc = new Scanner(System.in);
        Menu menu = new Menu(sc);

        System.out.println("  +------------------------------------------+");
        System.out.println("  |   NiceSystem - NeoEletro  v1.0           |");
        System.out.println("  |   Sistema de Gestao de Pedidos           |");
        System.out.println("  +------------------------------------------+");

        try {
            while (menu.exibirPrincipal()) { /* continua ate o usuario escolher 0 */ }
        } catch (SistemaException e) {
            System.out.println("  [ERRO BD] " + e.getMessage());
            if (e.getCause() != null) System.out.println("  Detalhe: " + e.getCause().getMessage());
        }

        processador.parar();
        System.out.println("\n  Encerrando NiceSystem... Ate logo!");
        sc.close();
    }
}