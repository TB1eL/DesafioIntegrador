package ui;

import dao.ClienteDAO;
import dao.ItemPedidoDTO;
import dao.PedidoDAO;
import dao.ProdutoDAO;
import exception.SistemaException;
import model.CategoriaProduto;
import model.Cliente;
import model.Pedido;
import model.Produto;
import service.ValidacaoService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Menu {

    private final Scanner sc;
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final PedidoDAO  pedidoDAO  = new PedidoDAO();

    public Menu(Scanner sc) {
        this.sc = sc;
    }

    // ----------------------------------------------------------------
    // MENU PRINCIPAL
    // ----------------------------------------------------------------
    public boolean exibirPrincipal() {
        System.out.println("\n  +------------------------------------------+");
        System.out.println("  |            MENU PRINCIPAL                |");
        System.out.println("  +------------------------------------------+");
        System.out.println("  |  1. Clientes                             |");
        System.out.println("  |  2. Produtos                             |");
        System.out.println("  |  3. Pedidos                              |");
        System.out.println("  |  4. Relatorios                           |");
        System.out.println("  |  0. Sair                                 |");
        System.out.println("  +------------------------------------------+");
        System.out.print("  > Opcao: ");

        switch (sc.nextLine().trim()) {
            case "1" -> menuClientes();
            case "2" -> menuProdutos();
            case "3" -> menuPedidos();
            case "4" -> menuRelatorios();
            case "0" -> { return false; }
            default  -> System.out.println("  [!] Opcao invalida.");
        }
        return true;
    }

    // ----------------------------------------------------------------
    // CLIENTES
    // ----------------------------------------------------------------
    private void menuClientes() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n  +------------------------------+");
            System.out.println("  |    GESTAO DE CLIENTES        |");
            System.out.println("  +------------------------------+");
            System.out.println("  |  1. Listar                   |");
            System.out.println("  |  2. Cadastrar                |");
            System.out.println("  |  3. Atualizar                |");
            System.out.println("  |  4. Remover                  |");
            System.out.println("  |  0. Voltar                   |");
            System.out.println("  +------------------------------+");
            System.out.print("  > Opcao: ");
            switch (sc.nextLine().trim()) {
                case "1" -> listarClientes();
                case "2" -> cadastrarCliente();
                case "3" -> atualizarCliente();
                case "4" -> removerCliente();
                case "0" -> voltar = true;
                default  -> System.out.println("  [!] Opcao invalida.");
            }
        }
    }

    private void listarClientes() {
        try {
            List<Cliente> lista = clienteDAO.listarTodos();
            System.out.println("\n  --- Clientes ---");
            if (lista.isEmpty()) { System.out.println("  Nenhum cliente cadastrado."); return; }
            lista.forEach(c -> System.out.println("  " + c));
        } catch (Exception e) { throw SistemaException.deBanco("Erro ao listar clientes.", e); }
    }

    private void cadastrarCliente() {
        try {
            System.out.print("  Nome: ");
            String nome = sc.nextLine().trim();
            ValidacaoService.validarNome(nome);

            System.out.print("  E-mail: ");
            String email = sc.nextLine().trim();
            ValidacaoService.validarEmail(email);

            clienteDAO.inserir(nome, email);
            System.out.println("  [OK] Cliente cadastrado!");
        } catch (SistemaException e) {
            System.out.println("  [ERRO] " + e.getMessage());
        } catch (Exception e) { throw SistemaException.deBanco("Erro ao cadastrar cliente.", e); }
    }

    private void atualizarCliente() {
        try {
            System.out.print("  ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());

            Optional<Cliente> opt = clienteDAO.buscarPorId(id);
            if (opt.isEmpty()) { System.out.println("  [!] Cliente nao encontrado."); return; }
            Cliente c = opt.get();
            System.out.println("  Atual: " + c);

            System.out.print("  Novo nome (Enter para manter): ");
            String nome = sc.nextLine().trim();
            if (nome.isBlank()) nome = c.getNome();
            ValidacaoService.validarNome(nome);

            System.out.print("  Novo e-mail (Enter para manter): ");
            String email = sc.nextLine().trim();
            if (email.isBlank()) email = c.getEmail();
            ValidacaoService.validarEmail(email);

            System.out.println(clienteDAO.atualizar(id, nome, email) ? "  [OK] Atualizado!" : "  [!] Nenhum registro alterado.");
        } catch (SistemaException e) {
            System.out.println("  [ERRO] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [ERRO] ID invalido.");
        } catch (Exception e) { throw SistemaException.deBanco("Erro ao atualizar cliente.", e); }
    }

    private void removerCliente() {
        try {
            System.out.print("  ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.println(clienteDAO.deletar(id) ? "  [OK] Cliente removido." : "  [!] Cliente nao encontrado.");
        } catch (NumberFormatException e) {
            System.out.println("  [ERRO] ID invalido.");
        } catch (Exception e) { throw SistemaException.deBanco("Erro ao remover cliente.", e); }
    }

    // ----------------------------------------------------------------
    // PRODUTOS
    // ----------------------------------------------------------------
    private void menuProdutos() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n  +------------------------------+");
            System.out.println("  |    GESTAO DE PRODUTOS        |");
            System.out.println("  +------------------------------+");
            System.out.println("  |  1. Listar                   |");
            System.out.println("  |  2. Cadastrar                |");
            System.out.println("  |  3. Atualizar                |");
            System.out.println("  |  4. Remover                  |");
            System.out.println("  |  0. Voltar                   |");
            System.out.println("  +------------------------------+");
            System.out.print("  > Opcao: ");
            switch (sc.nextLine().trim()) {
                case "1" -> listarProdutos();
                case "2" -> cadastrarProduto();
                case "3" -> atualizarProduto();
                case "4" -> removerProduto();
                case "0" -> voltar = true;
                default  -> System.out.println("  [!] Opcao invalida.");
            }
        }
    }

    private void listarProdutos() {
        try {
            List<Produto> lista = produtoDAO.listarTodos();
            System.out.println("\n  --- Produtos ---");
            if (lista.isEmpty()) { System.out.println("  Nenhum produto cadastrado."); return; }
            lista.forEach(p -> System.out.println("  " + p));
        } catch (Exception e) { throw SistemaException.deBanco("Erro ao listar produtos.", e); }
    }

    private void cadastrarProduto() {
        try {
            System.out.print("  Nome: ");
            String nome = sc.nextLine().trim();
            ValidacaoService.validarNome(nome);

            System.out.print("  Preco (ex: 99.90): ");
            BigDecimal preco = new BigDecimal(sc.nextLine().trim().replace(",", "."));
            ValidacaoService.validarPreco(preco);

            System.out.print("  Estoque: ");
            int estoque = Integer.parseInt(sc.nextLine().trim());
            ValidacaoService.validarEstoque(estoque);

            produtoDAO.inserir(nome, preco, estoque, selecionarCategoria());
            System.out.println("  [OK] Produto cadastrado!");
        } catch (SistemaException e) {
            System.out.println("  [ERRO] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [ERRO] Valor numerico invalido.");
        } catch (Exception e) { throw SistemaException.deBanco("Erro ao cadastrar produto.", e); }
    }

    private void atualizarProduto() {
        try {
            System.out.print("  ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());

            Optional<Produto> opt = produtoDAO.buscarPorId(id);
            if (opt.isEmpty()) { System.out.println("  [!] Produto nao encontrado."); return; }
            Produto p = opt.get();
            System.out.println("  Atual: " + p);

            System.out.print("  Novo nome (Enter para manter): ");
            String nome = sc.nextLine().trim();
            if (nome.isBlank()) nome = p.getNome();
            ValidacaoService.validarNome(nome);

            System.out.print("  Novo preco (Enter para manter): ");
            String ps = sc.nextLine().trim();
            BigDecimal preco = ps.isBlank() ? p.getPreco() : new BigDecimal(ps.replace(",", "."));
            ValidacaoService.validarPreco(preco);

            System.out.print("  Novo estoque (Enter para manter): ");
            String es = sc.nextLine().trim();
            int estoque = es.isBlank() ? p.getEstoque() : Integer.parseInt(es);
            ValidacaoService.validarEstoque(estoque);

            CategoriaProduto cat = p.getCategoria();
            System.out.print("  Alterar categoria? (S/N): ");
            if (sc.nextLine().trim().equalsIgnoreCase("S")) cat = selecionarCategoria();

            System.out.println(produtoDAO.atualizar(id, nome, preco, estoque, cat) ? "  [OK] Atualizado!" : "  [!] Nenhum registro alterado.");
        } catch (SistemaException e) {
            System.out.println("  [ERRO] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [ERRO] Valor numerico invalido.");
        } catch (Exception e) { throw SistemaException.deBanco("Erro ao atualizar produto.", e); }
    }

    private void removerProduto() {
        try {
            System.out.print("  ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.println(produtoDAO.deletar(id) ? "  [OK] Produto removido." : "  [!] Produto nao encontrado.");
        } catch (NumberFormatException e) {
            System.out.println("  [ERRO] ID invalido.");
        } catch (Exception e) { throw SistemaException.deBanco("Erro ao remover produto.", e); }
    }

    private CategoriaProduto selecionarCategoria() {
        CategoriaProduto[] cats = CategoriaProduto.values();
        System.out.println("  Categorias:");
        for (int i = 0; i < cats.length; i++)
            System.out.printf("    %d. %s%n", i + 1, cats[i]);
        System.out.print("  Escolha: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (idx < 0 || idx >= cats.length) throw SistemaException.deValidacao("Categoria invalida.");
        return cats[idx];
    }

    // ----------------------------------------------------------------
    // PEDIDOS
    // ----------------------------------------------------------------
    private void menuPedidos() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n  +------------------------------+");
            System.out.println("  |    GESTAO DE PEDIDOS         |");
            System.out.println("  +------------------------------+");
            System.out.println("  |  1. Listar pedidos           |");
            System.out.println("  |  2. Criar pedido             |");
            System.out.println("  |  0. Voltar                   |");
            System.out.println("  +------------------------------+");
            System.out.print("  > Opcao: ");
            switch (sc.nextLine().trim()) {
                case "1" -> listarPedidos();
                case "2" -> criarPedido();
                case "0" -> voltar = true;
                default  -> System.out.println("  [!] Opcao invalida.");
            }
        }
    }

    private void listarPedidos() {
        try {
            List<Pedido> lista = pedidoDAO.listarTodos();
            System.out.println("\n  --- Pedidos ---");
            if (lista.isEmpty()) { System.out.println("  Nenhum pedido encontrado."); return; }
            lista.forEach(p -> System.out.println("  " + p + "\n"));
        } catch (Exception e) { throw SistemaException.deBanco("Erro ao listar pedidos.", e); }
    }

    private void criarPedido() {
        try {
            System.out.print("  ID do cliente: ");
            String idStr = sc.nextLine().trim();
            if (idStr.isBlank()) { System.out.println("  [!] ID nao pode ser vazio."); return; }
            int clienteId = Integer.parseInt(idStr);

            if (!clienteDAO.existePorId(clienteId)) {
                System.out.println("  [!] Cliente ID " + clienteId + " nao encontrado.");
                return;
            }

            listarProdutos();

            List<ItemPedidoDTO> itens = new ArrayList<>();
            while (true) {
                System.out.print("\n  ID do produto (0 para finalizar): ");
                String entrada = sc.nextLine().trim();
                if (entrada.isBlank()) { System.out.println("  [!] Use 0 para finalizar."); continue; }

                int prodId;
                try { prodId = Integer.parseInt(entrada); }
                catch (NumberFormatException e) { System.out.println("  [!] Digite apenas numeros."); continue; }

                if (prodId == 0) break;

                Optional<Produto> opt = produtoDAO.buscarPorId(prodId);
                if (opt.isEmpty()) { System.out.println("  [!] Produto nao encontrado."); continue; }

                System.out.print("  Quantidade: ");
                String qtdStr = sc.nextLine().trim();
                if (qtdStr.isBlank()) { System.out.println("  [!] Informe a quantidade."); continue; }

                int qtd;
                try { qtd = Integer.parseInt(qtdStr); }
                catch (NumberFormatException e) { System.out.println("  [!] Quantidade invalida."); continue; }

                try { ValidacaoService.validarQuantidade(qtd); }
                catch (SistemaException e) { System.out.println("  [ERRO] " + e.getMessage()); continue; }

                Produto prod = opt.get();
                if (qtd > prod.getEstoque()) {
                    System.out.printf("  [!] Estoque insuficiente para '%s'. Disponivel: %d%n", prod.getNome(), prod.getEstoque());
                    continue;
                }

                itens.add(new ItemPedidoDTO(prodId, qtd));
                System.out.printf("  [+] %s x%d adicionado.%n", prod.getNome(), qtd);
            }

            if (itens.isEmpty()) { System.out.println("  [!] Pedido cancelado: nenhum item."); return; }

            int pedidoId = pedidoDAO.criarPedido(clienteId, itens);
            System.out.printf("  [OK] Pedido #%d criado e colocado na FILA!%n", pedidoId);

        } catch (SistemaException e) {
            System.out.println("  [ERRO] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [ERRO] ID de cliente invalido.");
        } catch (Exception e) {
            throw SistemaException.deBanco("Erro ao criar pedido.", e);
        }
    }

    // ----------------------------------------------------------------
    // RELATORIOS
    // ----------------------------------------------------------------
    private void menuRelatorios() {
        boolean voltar = false;
        while (!voltar) {
            System.out.println("\n  +--------------------------------------+");
            System.out.println("  |       RELATORIOS GERENCIAIS          |");
            System.out.println("  +--------------------------------------+");
            System.out.println("  |  1. Vendas por Categoria             |");
            System.out.println("  |  2. Ranking de Clientes              |");
            System.out.println("  |  3. Produtos com Estoque Critico     |");
            System.out.println("  |  0. Voltar                           |");
            System.out.println("  +--------------------------------------+");
            System.out.print("  > Opcao: ");
            try {
                switch (sc.nextLine().trim()) {
                    case "1" -> { System.out.println("\n  === Vendas por Categoria ===");      pedidoDAO.relatorioVendasPorCategoria(); }
                    case "2" -> { System.out.println("\n  === Ranking de Clientes ===");        pedidoDAO.relatorioRankingClientes(); }
                    case "3" -> { System.out.println("\n  === Estoque Critico (< 5 un.) ==="); pedidoDAO.relatorioBaixoEstoque(); }
                    case "0" -> voltar = true;
                    default  -> System.out.println("  [!] Opcao invalida.");
                }
            } catch (Exception e) { throw SistemaException.deBanco("Erro ao gerar relatorio.", e); }
        }
    }
}