package net.byebye.lojaplacas.events;

import net.byebye.balance.BalanceAPI;
import net.byebye.balance.Economy;
import net.byebye.lojaplacas.Loja;
import net.byebye.lojaplacas.LojaTemp;
import net.byebye.lojaplacas.Main;
import net.byebye.lojaplacas.ServidorLoja;
import net.byebye.lojaplacas.gui.LojaGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Events implements Listener {

    private final Main plugin = Main.getInstance();
    private final Economy eco = BalanceAPI.getEconomy();

    @EventHandler
    public void onSignCreate(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (event.getLine(0).equalsIgnoreCase("minhaloja")) {
            if (!player.hasPermission("minhaloja.criar")) {
                player.sendMessage(ChatColor.RED + "Você não tem permissão para criar uma loja!");
                event.setCancelled(true);
                return;
            }

            // Criar loja temporária para verificar se há baú adjacente
            LojaTemp lojaTemp = new LojaTemp(event.getBlock().getLocation());

            // Verificar se há um baú adjacente à placa
            if (!lojaTemp.temBauAdjacente()) {
                player.sendMessage(ChatColor.RED + "Você precisa colocar a placa de loja em um baú!");
                event.setCancelled(true);
                return;
            }

            // Verificar modo desenvolvedor (para lojas do servidor)
            boolean isServidorLoja = plugin.isModoDesenvolvedor() && player.hasPermission("minhaloja.admin");

            // Configurar a placa
            event.setLine(0, ChatColor.DARK_BLUE + "[MinhaLoja]");
            event.setLine(1, ChatColor.GRAY + "Configurando...");
            event.setLine(2, "");

            if (isServidorLoja) {
                event.setLine(3, ChatColor.RED + "SERVIDOR");
                plugin.getLogger().info("[MinhaLoja] " + player.getName() + " está criando uma loja do SERVIDOR em modo desenvolvedor.");
            } else {
                event.setLine(3, ChatColor.GRAY + player.getName());
            }

            // Adicionar loja temporária para configuração
            plugin.addLojaTemp(player.getUniqueId(), lojaTemp);

            // Registrar o UUID do jogador no log para depuração
            plugin.getLogger().info("[MinhaLoja] UUID do jogador registrado para loja temporária: " + player.getUniqueId());

            // Verificar se a loja temporária foi adicionada corretamente
            if (plugin.getLojaTemp(player.getUniqueId()) != null) {
                plugin.getLogger().info("[MinhaLoja] Loja temporária registrada com sucesso para o jogador " + player.getName());
            } else {
                plugin.getLogger().warning("[MinhaLoja] FALHA ao registrar loja temporária para o jogador " + player.getName());
            }

            // Abrir GUI de configuração após um pequeno delay (para garantir que a placa foi criada)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Verificar novamente se a loja temporária ainda existe
                if (plugin.getLojaTemp(player.getUniqueId()) != null) {
                    plugin.getLogger().info("[MinhaLoja] Abrindo GUI de configuração para " + player.getName());
                    LojaGUI.abrirConfiguracaoGUI(player);
                } else {
                    plugin.getLogger().warning("[MinhaLoja] Loja temporária não encontrada ao tentar abrir GUI para " + player.getName());
                    // Criar loja temporária novamente se necessário
                    plugin.addLojaTemp(player.getUniqueId(), lojaTemp);
                    LojaGUI.abrirConfiguracaoGUI(player);
                }
            }, 10L); // Aumentei para 10 ticks para garantir que tudo seja processado

            if (isServidorLoja) {
                player.sendMessage(ChatColor.GREEN + "Placa de loja do SERVIDOR criada! Configure a loja.");
                // Enviar mensagem de uso para ajudar o jogador
                player.sendMessage(ChatColor.YELLOW + "Clique com o botão direito na placa para abrir o menu de configuração se não abrir automaticamente.");
            } else {
                player.sendMessage(ChatColor.GREEN + "Placa de loja criada! Configure sua loja.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        // Verificar se é uma placa
        if (!(block.getState() instanceof Sign)) return;

        Sign sign = (Sign) block.getState();
        if (!sign.getLine(0).equals(ChatColor.DARK_BLUE + "[MinhaLoja]")) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        // Verificar se a loja está configurada ou não
        if (sign.getLine(1).equals(ChatColor.GRAY + "Configurando...")) {
            // Se alguém está configurando a loja, verificar se é o jogador atual
            if (plugin.getLojasTemp().containsKey(player.getUniqueId())) {
                // É o jogador atual, abrir interface de configuração
                plugin.getLogger().info("[MinhaLoja] Jogador " + player.getName() + " está abrindo GUI para loja já registrada.");
                LojaGUI.abrirConfiguracaoGUI(player);
            } else {
                // É outro jogador, verificar se é modo desenvolvedor e jogador é admin
                // Obter UUID do dono através da placa (linha 3)
                String linha3 = sign.getLine(3);
                boolean isServidorLoja = linha3.contains("SERVIDOR");

                if (isServidorLoja && player.hasPermission("minhaloja.admin")) {
                    // É admin e loja do servidor, pode configurar
                    Location loc = sign.getLocation();
                    LojaTemp lojaTemp = new LojaTemp(loc);
                    plugin.getLogger().info("[MinhaLoja] Admin " + player.getName() + " está assumindo configuração de loja do SERVIDOR.");
                    plugin.addLojaTemp(player.getUniqueId(), lojaTemp);

                    // Abrir interface com delay para garantir registro
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (plugin.getLojaTemp(player.getUniqueId()) != null) {
                            LojaGUI.abrirConfiguracaoGUI(player);
                        } else {
                            player.sendMessage(ChatColor.RED + "Erro ao registrar loja. Tente novamente.");
                        }
                    }, 2L);
                } else {
                    player.sendMessage(ChatColor.RED + "Esta loja ainda está sendo configurada por outro jogador.");
                }
            }
            return;
        }

        // Verificar se a loja existe no arquivo de configuração
        Location location = sign.getLocation();
        String locKey = location.getWorld().getName() + "," +
                location.getBlockX() + "," +
                location.getBlockY() + "," +
                location.getBlockZ();

        ConfigurationSection lojaSection = plugin.getLojasConfig().getConfigurationSection("lojas." + locKey);
        if (lojaSection == null) {
            player.sendMessage(ChatColor.RED + "Esta loja não existe mais.");
            return;
        }

        // Criar objeto Loja com base no tipo (normal ou servidor)
        UUID donoUUID = UUID.fromString(lojaSection.getString("dono"));
        boolean isServidorLoja = donoUUID.equals(ServidorLoja.SERVIDOR_UUID);

        // Recuperar localização do baú
        String chestWorldName = lojaSection.getString("chestWorld");
        double chestX = lojaSection.getDouble("chestX");
        double chestY = lojaSection.getDouble("chestY");
        double chestZ = lojaSection.getDouble("chestZ");
        Location chestLocation = new Location(Bukkit.getWorld(chestWorldName), chestX, chestY, chestZ);

        // Recuperar dados do item e loja
        ItemStack item = (ItemStack) lojaSection.get("item");
        int quantidade = lojaSection.getInt("quantidade");
        double preco = lojaSection.getDouble("preco");
        LojaTemp.TipoLoja tipo = LojaTemp.TipoLoja.valueOf(lojaSection.getString("tipo"));

        Loja loja;

        if (isServidorLoja) {
            loja = new ServidorLoja(
                    location,
                    item,
                    preco,
                    quantidade,
                    tipo,
                    chestLocation
            );
        } else {
            loja = new Loja(
                    donoUUID,
                    location,
                    item,
                    preco,
                    quantidade,
                    tipo,
                    chestLocation
            );
        }

        if (player.getUniqueId().equals(donoUUID) || (isServidorLoja && player.hasPermission("minhaloja.admin"))) {
            // O dono da loja (ou admin para lojas do servidor) pode configurá-la
            LojaTemp lojaTemp = new LojaTemp(location);
            lojaTemp.setItem(item);
            lojaTemp.setPreco(preco);
            lojaTemp.setQuantidade(quantidade);
            lojaTemp.setTipo(tipo);
            lojaTemp.setChestLocation(chestLocation);

            plugin.addLojaTemp(player.getUniqueId(), lojaTemp);
            LojaGUI.abrirConfiguracaoGUI(player);
        } else {
            // Outros jogadores interagem com a loja
            if (tipo == LojaTemp.TipoLoja.VENDA) {
                // Loja vende itens para o jogador
                comprarDaLoja(player, loja);
            } else {
                // Loja compra itens do jogador
                venderParaLoja(player, loja);
            }
        }
    }

    public void comprarDaLoja(Player player, Loja loja) {
        boolean isServidorLoja = ServidorLoja.isServidorLoja(loja);

        // Verificar se a loja tem estoque suficiente (sempre verdadeiro para lojas do servidor)
        if (!isServidorLoja && !loja.temEstoque()) {
            player.sendMessage(ChatColor.RED + "Esta loja está sem estoque no momento.");
            return;
        }

        // Verificar se o jogador tem dinheiro suficiente
        double preco = loja.getPreco();
        if (eco.getSaldo(player) < preco) {
            player.sendMessage(ChatColor.RED + "Você não tem dinheiro suficiente para comprar este item.");
            return;
        }

        // Verificar se o jogador tem espaço no inventário
        ItemStack item = loja.getItem().clone();
        int quantidade = loja.getQuantidade();
        item.setAmount(quantidade);

        if (!temEspacoNoInventario(player, item)) {
            player.sendMessage(ChatColor.RED + "Você não tem espaço suficiente no inventário.");
            return;
        }

        // Remover dinheiro do jogador
        eco.removeSaldo(player, preco);

        // Verificar se é uma loja do servidor
        UUID donoUUID = loja.getDono();

        // Adicionar dinheiro ao dono da loja (se não for do servidor)
        if (!isServidorLoja) {
            // Sempre armazenar o pagamento no sistema de pagamentos offline
            // Isso será processado quando o jogador entrar novamente
            Map<String, Object> pagamentoOffline = new HashMap<>();
            pagamentoOffline.put("valor", preco);
            pagamentoOffline.put("mensagem", player.getName() + " comprou " + quantidade + "x " +
                    item.getType().name() + " por " + preco + " da sua loja.");

            plugin.getLojasConfig().set("pagamentos_offline." + donoUUID.toString() + "." +
                    System.currentTimeMillis(), pagamentoOffline);
            plugin.saveLojasConfig();

            // Se o dono estiver online, notificar e dar o dinheiro imediatamente
            Player dono = Bukkit.getPlayer(donoUUID);
            if (dono != null && dono.isOnline()) {
                eco.addSaldo(dono, preco);
                dono.sendMessage(ChatColor.GREEN + player.getName() + " comprou " + quantidade + "x " +
                        item.getType().name() + " por " + preco + " da sua loja.");
            }
        } else {
            plugin.getLogger().info("[MinhaLoja] " + player.getName() + " comprou " + quantidade + "x " +
                    item.getType().name() + " por " + preco + " da loja do SERVIDOR.");
        }

        // Dar os itens ao jogador
        player.getInventory().addItem(item);

        // Remover itens do baú (não necessário para lojas do servidor)
        if (!isServidorLoja) {
            removerItensDoBau(loja.getBau().getInventory(), item, quantidade);
        }

        if (isServidorLoja) {
            player.sendMessage(ChatColor.GREEN + "Você comprou " + quantidade + "x " +
                    item.getType().name() + " por " + preco + " da loja do SERVIDOR.");
        } else {
            player.sendMessage(ChatColor.GREEN + "Você comprou " + quantidade + "x " +
                    item.getType().name() + " por " + preco + ".");
        }
    }

    public void venderParaLoja(Player player, Loja loja) {
        ItemStack item = loja.getItem().clone();
        int quantidade = loja.getQuantidade();
        double preco = loja.getPreco();
        UUID donoUUID = loja.getDono();
        boolean isServidorLoja = ServidorLoja.isServidorLoja(loja);

        // Verificar se o jogador tem os itens para vender
        if (!temItensParaVender(player, item, quantidade)) {
            player.sendMessage(ChatColor.RED + "Você não tem itens suficientes para vender.");
            return;
        }

        // Verificar se o baú da loja tem espaço para armazenar os itens (sempre verdadeiro para lojas do servidor)
        if (!isServidorLoja && !loja.temEspacoNoEstoque()) {
            player.sendMessage(ChatColor.RED + "Esta loja está com o estoque cheio no momento.");
            return;
        }

        // Realizar a transação dependendo do tipo de loja
        if (isServidorLoja) {
            // Loja do servidor: não precisa verificar saldo, só registrar no log
            plugin.getLogger().info("[MinhaLoja] " + player.getName() + " vendeu " + quantidade + "x " +
                    item.getType().name() + " por " + preco + " para loja do SERVIDOR.");

            // Dar dinheiro ao jogador diretamente
            eco.addSaldo(player, preco);
        } else {
            // Para transações com lojas offline, verificamos o saldo offline
            boolean transacaoSucesso = false;

            // Verificar se o dono está online
            Player dono = Bukkit.getPlayer(donoUUID);

            if (dono != null && dono.isOnline()) {
                // Dono online - verificar saldo e completar transação imediatamente
                if (eco.getSaldo(dono) < preco) {
                    player.sendMessage(ChatColor.RED + "O dono da loja não tem dinheiro suficiente.");
                    return;
                }

                // Remover dinheiro do dono
                eco.removeSaldo(dono, preco);
                dono.sendMessage(ChatColor.GREEN + player.getName() + " vendeu " + quantidade + "x " +
                        item.getType().name() + " por " + preco + " para sua loja.");

                transacaoSucesso = true;
            } else {
                // Dono offline - usar sistema de saldo offline
                // 1. Verificar se há uma entrada de saldo offline para o dono
                String saldoOfflinePath = "saldos_offline." + donoUUID.toString();
                double saldoOffline = plugin.getLojasConfig().getDouble(saldoOfflinePath, -1);

                // 2. Se não houver saldo offline registrado, criar um com um valor base alto
                if (saldoOffline == -1) {
                    // Criar um saldo offline inicial de 10000 (configurável)
                    saldoOffline = 10000.0;
                    plugin.getLojasConfig().set(saldoOfflinePath, saldoOffline);
                    plugin.saveLojasConfig();
                }

                // 3. Verificar se o saldo offline é suficiente
                if (saldoOffline < preco) {
                    player.sendMessage(ChatColor.RED + "O dono da loja não tem dinheiro suficiente em seu saldo offline.");
                    return;
                }

                // 4. Atualizar saldo offline
                plugin.getLojasConfig().set(saldoOfflinePath, saldoOffline - preco);

                // 5. Registrar compra para quando o dono ficar online
                Map<String, Object> compraOffline = new HashMap<>();
                compraOffline.put("item", item);
                compraOffline.put("quantidade", quantidade);
                compraOffline.put("mensagem", player.getName() + " vendeu " + quantidade + "x " +
                        item.getType().name() + " por " + preco + " para sua loja.");

                plugin.getLojasConfig().set("compras_offline." + donoUUID.toString() + "." +
                        System.currentTimeMillis(), compraOffline);

                plugin.saveLojasConfig();
                transacaoSucesso = true;
            }

            // Se a transação for bem-sucedida, continuar com o processo
            if (transacaoSucesso) {
                // Remover itens do jogador
                removerItensDoInventario(player, item, quantidade);

                // Adicionar itens ao baú
                item.setAmount(quantidade);
                adicionarItensNoBau(loja.getBau().getInventory(), item, quantidade);

                // Dar dinheiro ao jogador
                eco.addSaldo(player, preco);

                player.sendMessage(ChatColor.GREEN + "Você vendeu " + quantidade + "x " +
                        item.getType().name() + " por " + preco + ".");
            }

            return;
        }

        // Código específico para lojas do servidor
        // Remover itens do jogador
        removerItensDoInventario(player, item, quantidade);

        // Dar dinheiro ao jogador
        eco.addSaldo(player, preco);

        player.sendMessage(ChatColor.GREEN + "Você vendeu " + quantidade + "x " +
                item.getType().name() + " por " + preco + " para a loja do SERVIDOR.");
    }

    private void removerItensDoBau(Inventory inv, ItemStack item, int quantidade) {
        int restante = quantidade;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack slot = inv.getItem(i);
            if (slot != null && slot.isSimilar(item)) {
                int q = slot.getAmount();
                if (q <= restante) {
                    inv.setItem(i, null);
                    restante -= q;
                } else {
                    slot.setAmount(q - restante);
                    inv.setItem(i, slot);
                    break;
                }
            }
        }
    }


    private boolean adicionarItensNoBau(Inventory inv, ItemStack item, int quantidade) {
        int restante = quantidade;
        ItemStack itemCopia = item.clone();

        // Primeiro: tentar empilhar nos slots existentes
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack slot = inv.getItem(i);
            if (slot != null && slot.isSimilar(item)) {
                int max = item.getMaxStackSize();
                int disponivel = max - slot.getAmount();

                if (disponivel > 0) {
                    int aAdicionar = Math.min(disponivel, restante);
                    slot.setAmount(slot.getAmount() + aAdicionar);
                    inv.setItem(i, slot);
                    restante -= aAdicionar;
                }

                if (restante <= 0) return true;
            }
        }

        // Segundo: preencher slots vazios
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                int aAdicionar = Math.min(itemCopia.getMaxStackSize(), restante);
                ItemStack novo = itemCopia.clone();
                novo.setAmount(aAdicionar);
                inv.setItem(i, novo);
                restante -= aAdicionar;

                if (restante <= 0) return true;
            }
        }

        // Se ainda sobrou, falhou
        return false;
    }


    private boolean temEspacoNoInventario(Player player, ItemStack item) {
        // Verificar se cabe no inventário
        Map<Integer, ItemStack> sobras = player.getInventory().addItem(item.clone());
        if (!sobras.isEmpty()) {
            // Remover o item que tentamos adicionar
            for (ItemStack sobra : sobras.values()) {
                player.getInventory().removeItem(sobra);
            }
            return false;
        }

        // Remover o item que adicionamos para teste
        player.getInventory().removeItem(item.clone());
        return true;
    }

    private boolean temItensParaVender(Player player, ItemStack itemProcurado, int quantidadeProcurada) {
        int quantidadeEncontrada = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(itemProcurado)) {
                quantidadeEncontrada += item.getAmount();

                if (quantidadeEncontrada >= quantidadeProcurada) {
                    return true;
                }
            }
        }

        return false;
    }

    private void removerItensDoInventario(Player player, ItemStack itemParaRemover, int quantidadeParaRemover) {
        ItemStack[] itens = player.getInventory().getContents();
        int quantidadeRestante = quantidadeParaRemover;

        for (int i = 0; i < itens.length; i++) {
            ItemStack item = itens[i];

            if (item != null && item.isSimilar(itemParaRemover)) {
                int quantidade = item.getAmount();

                if (quantidade <= quantidadeRestante) {
                    // Remover todo o stack
                    player.getInventory().setItem(i, null);
                    quantidadeRestante -= quantidade;
                } else {
                    // Remover parte do stack
                    item.setAmount(quantidade - quantidadeRestante);
                    quantidadeRestante = 0;
                }

                if (quantidadeRestante == 0) {
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // Use getTitle() do view em vez de getName() do inventário para compatibilidade
        String titulo = event.getView().getTitle();

        // Cancelar qualquer clique em inventários personalizados
        if (titulo.equals(LojaGUI.TITULO_CONFIG) ||
                titulo.equals(LojaGUI.TITULO_CONFIG_SERVIDOR) ||
                titulo.equals(LojaGUI.TITULO_SELECAO_ITEM) ||
                titulo.contains("Lojas - Página") ||
                titulo.contains("Minhas Lojas - Página")) {

            // Cancelar o evento para impedir que os itens sejam movidos
            event.setCancelled(true);

            // Se não for uma ação válida, retornar
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            int slot = event.getRawSlot();

            // Processamento específico para cada tipo de inventário
            if (titulo.equals(LojaGUI.TITULO_CONFIG) || titulo.equals(LojaGUI.TITULO_CONFIG_SERVIDOR)) {
                if (slot == LojaGUI.SLOT_ITEM) {
                    // Selecionar item - abrir inventário de seleção
                    LojaGUI.abrirSelecaoItemGUI(player);
                } else if (slot == LojaGUI.SLOT_PRECO) {
                    // Configurar preço
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Digite o preço no chat:");
                    ChatListener chatListener = new ChatListener();
                    chatListener.aguardarConfiguracao(player, ChatListener.ConfigType.PRECO);
                } else if (slot == LojaGUI.SLOT_QUANTIDADE) {
                    // Configurar quantidade
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Digite a quantidade no chat:");
                    ChatListener chatListener = new ChatListener();
                    chatListener.aguardarConfiguracao(player, ChatListener.ConfigType.QUANTIDADE);
                } else if (slot == LojaGUI.SLOT_TIPO) {
                    // Alternar tipo de loja
                    LojaTemp lojaTemp = plugin.getLojaTemp(player.getUniqueId());
                    if (lojaTemp != null) {
                        lojaTemp.alternarTipo();
                        LojaGUI.abrirConfiguracaoGUI(player);
                    }
                } else if (slot == LojaGUI.SLOT_CONFIRMAR) {
                    // Confirmar configuração
                    LojaTemp lojaTemp = plugin.getLojaTemp(player.getUniqueId());
                    if (lojaTemp != null) {
                        if (!lojaTemp.temBauAdjacente()) {
                            player.sendMessage(ChatColor.RED + "Não há um baú adjacente à sua placa de loja!");
                            return;
                        }

                        if (lojaTemp.isConfigCompleta()) {
                            // Verificar se o baú está vazio (para lojas de venda) - não necessário para lojas do servidor
                            boolean isServidorLoja = plugin.isModoDesenvolvedor() && player.hasPermission("minhaloja.admin");

                            if (lojaTemp.getTipo() == LojaTemp.TipoLoja.VENDA && !isServidorLoja) {
                                Chest chest = lojaTemp.getBau();
                                if (chest != null) {
                                    ItemStack itemCheck = lojaTemp.getItem().clone();
                                    itemCheck.setAmount(lojaTemp.getQuantidade());

                                    UUID donoUUID = player.getUniqueId();

                                    Loja lojaTemp2 = new Loja(
                                            donoUUID,
                                            lojaTemp.getPlacaLocation(),
                                            lojaTemp.getItem(),
                                            lojaTemp.getPreco(),
                                            lojaTemp.getQuantidade(),
                                            lojaTemp.getTipo(),
                                            lojaTemp.getChestLocation()
                                    );

                                    if (!lojaTemp2.temEstoque()) {
                                        player.sendMessage(ChatColor.RED + "Você precisa ter os itens no baú para criar uma loja de venda!");
                                        return;
                                    }
                                }
                            }

                            // Verifica se é modo desenvolvedor
                            boolean isServidorLoja2 = plugin.isModoDesenvolvedor() && player.hasPermission("minhaloja.admin");
                            UUID donoUUID = isServidorLoja2 ? ServidorLoja.SERVIDOR_UUID : player.getUniqueId();

                            // Salvar loja
                            Loja loja = new Loja(
                                    donoUUID,
                                    lojaTemp.getPlacaLocation(),
                                    lojaTemp.getItem(),
                                    lojaTemp.getPreco(),
                                    lojaTemp.getQuantidade(),
                                    lojaTemp.getTipo(),
                                    lojaTemp.getChestLocation()
                            );

                            // Salvar na configuração
                            Location loc = lojaTemp.getPlacaLocation();
                            String locKey = loc.getWorld().getName() + "," +
                                    loc.getBlockX() + "," +
                                    loc.getBlockY() + "," +
                                    loc.getBlockZ();

                            String tipoString = lojaTemp.getTipo() == LojaTemp.TipoLoja.VENDA ? "Vende" : "Compra";
                            String itemName = lojaTemp.getItem().getType().name();

                            // Atualizar placa
                            if (loc.getBlock().getState() instanceof Sign) {
                                Sign sign = (Sign) loc.getBlock().getState();
                                sign.setLine(0, ChatColor.DARK_BLUE + "[MinhaLoja]");
                                sign.setLine(1, ChatColor.GREEN + tipoString);
                                sign.setLine(2, ChatColor.GOLD + itemName);

                                // Mostrar preço em vez do nome do jogador
                                sign.setLine(3, ChatColor.YELLOW + "$" + lojaTemp.getPreco());

                                sign.update();
                            }

                            // Salvar dados da loja
                            ConfigurationSection section = plugin.getLojasConfig().createSection("lojas." + locKey);

                            // Verificar se é loja do servidor ou de jogador
                            boolean isServidorLoja3 = plugin.isModoDesenvolvedor() && player.hasPermission("minhaloja.admin");
                            if (isServidorLoja3) {
                                section.set("dono", ServidorLoja.SERVIDOR_UUID.toString());
                            } else {
                                section.set("dono", player.getUniqueId().toString());
                            }

                            section.set("item", lojaTemp.getItem());
                            section.set("preco", lojaTemp.getPreco());
                            section.set("quantidade", lojaTemp.getQuantidade());
                            section.set("tipo", lojaTemp.getTipo().name());

                            // Salvar localização do baú
                            Location chestLoc = lojaTemp.getChestLocation();
                            section.set("chestWorld", chestLoc.getWorld().getName());
                            section.set("chestX", chestLoc.getX());
                            section.set("chestY", chestLoc.getY());
                            section.set("chestZ", chestLoc.getZ());

                            plugin.saveLojasConfig();

                            // Remover loja temporária
                            plugin.removeLojaTemp(player.getUniqueId());

                            player.closeInventory();

                            // Verificar se é loja do servidor ou de jogador
                            boolean isServidorLoja4 = plugin.isModoDesenvolvedor() && player.hasPermission("minhaloja.admin");
                            if (isServidorLoja4) {
                                player.sendMessage(ChatColor.GREEN + "Loja do SERVIDOR configurada com sucesso!");
                                plugin.getLogger().info("[MinhaLoja] " + player.getName() + " criou uma loja do SERVIDOR em " + locKey);
                            } else {
                                player.sendMessage(ChatColor.GREEN + "Loja configurada com sucesso!");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Configure todos os aspectos da loja antes de confirmar!");
                        }
                    }
                } else if (slot == LojaGUI.SLOT_CANCELAR) {
                    // Cancelar configuração
                    LojaTemp lojaTemp = plugin.getLojaTemp(player.getUniqueId());
                    if (lojaTemp != null) {
                        Location loc = lojaTemp.getPlacaLocation();
                        if (loc.getBlock().getState() instanceof Sign) {
                            loc.getBlock().breakNaturally();
                        }
                        plugin.removeLojaTemp(player.getUniqueId());
                    }

                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "Configuração da loja cancelada.");
                }
            } else if (titulo.equals(LojaGUI.TITULO_SELECAO_ITEM)) {
                // Só cancela o evento se for um clique no inventário superior (GUI)
                if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                    event.setCancelled(true);
                }

                // Selecionar item da loja
                if (event.getRawSlot() >= 54 || (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getRawSlot() >= 9)) {
                    if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                        ItemStack item = event.getCurrentItem().clone();
                        item.setAmount(1); // Salvar apenas 1 de quantidade

                        LojaTemp lojaTemp = plugin.getLojaTemp(player.getUniqueId());
                        if (lojaTemp != null) {
                            lojaTemp.setItem(item);
                            player.sendMessage(ChatColor.GREEN + "Item selecionado: " + item.getType().name());
                        }

                        // Voltar para o menu de configuração
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            LojaGUI.abrirConfiguracaoGUI(player);
                        }, 1L);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        String titulo = event.getView().getTitle();

        // Cancelar arrastar em inventários personalizados
        if (titulo.equals(LojaGUI.TITULO_CONFIG) ||
                titulo.equals(LojaGUI.TITULO_CONFIG_SERVIDOR) ||
                titulo.equals(LojaGUI.TITULO_SELECAO_ITEM) ||
                titulo.contains("Lojas - Página") ||
                titulo.contains("Minhas Lojas - Página")) {

            // Verificar se algum dos slots afetados está no inventário superior
            for (int slot : event.getRawSlots()) {
                if (slot < event.getView().getTopInventory().getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();

        // Use getTitle() do view em vez de getName() do inventário para compatibilidade
        String titulo = event.getView().getTitle();

        if (titulo.equals(LojaGUI.TITULO_SELECAO_ITEM)) {
            // Reabrir menu de configuração se fechou a seleção de item
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                LojaGUI.abrirConfiguracaoGUI(player);
            }, 1L);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Verificar se é uma placa de loja
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();

            if (!sign.getLine(0).equals(ChatColor.DARK_BLUE + "[MinhaLoja]")) return;

            Player player = event.getPlayer();

            // Verificar se a loja existe no arquivo de configuração
            Location location = sign.getLocation();
            String locKey = location.getWorld().getName() + "," +
                    location.getBlockX() + "," +
                    location.getBlockY() + "," +
                    location.getBlockZ();

            ConfigurationSection lojaSection = plugin.getLojasConfig().getConfigurationSection("lojas." + locKey);
            if (lojaSection == null) return;

            // Verificar se o jogador é o dono da loja ou admin
            UUID donoUUID = UUID.fromString(lojaSection.getString("dono"));
            boolean isServidorLoja = donoUUID.equals(ServidorLoja.SERVIDOR_UUID);

            if (isServidorLoja && !player.hasPermission("minhaloja.admin")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Você não pode quebrar uma loja do SERVIDOR!");
                return;
            } else if (!isServidorLoja && !player.getUniqueId().equals(donoUUID) && !player.hasPermission("minhaloja.admin")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Você não pode quebrar a loja de outro jogador!");
                return;
            }

            // Remover a loja do arquivo de configuração
            plugin.getLojasConfig().set("lojas." + locKey, null);
            plugin.saveLojasConfig();

            player.sendMessage(ChatColor.GREEN + "Loja removida com sucesso!");
        } else if (block.getState() instanceof Chest) {
            // Verificar se o baú faz parte de uma loja
            ConfigurationSection lojasSection = plugin.getLojasConfig().getConfigurationSection("lojas");
            if (lojasSection == null) return;

            for (String locKey : lojasSection.getKeys(false)) {
                ConfigurationSection lojaSection = lojasSection.getConfigurationSection(locKey);

                String chestWorldName = lojaSection.getString("chestWorld");
                double chestX = lojaSection.getDouble("chestX");
                double chestY = lojaSection.getDouble("chestY");
                double chestZ = lojaSection.getDouble("chestZ");

                Location chestLoc = new Location(Bukkit.getWorld(chestWorldName), chestX, chestY, chestZ);

                if (block.getLocation().equals(chestLoc)) {
                    // Verificar se o jogador é o dono da loja ou admin
                    UUID donoUUID = UUID.fromString(lojaSection.getString("dono"));
                    Player player = event.getPlayer();
                    boolean isServidorLoja = donoUUID.equals(ServidorLoja.SERVIDOR_UUID);

                    if (isServidorLoja && !player.hasPermission("minhaloja.admin")) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Este baú pertence a uma loja do SERVIDOR! Você não pode quebrá-lo.");
                        return;
                    } else if (!isServidorLoja && !player.getUniqueId().equals(donoUUID) && !player.hasPermission("minhaloja.admin")) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Este baú pertence a uma loja! Você não pode quebrá-lo.");
                        return;
                    }

                    // Remover a loja relacionada ao baú
                    plugin.getLojasConfig().set("lojas." + locKey, null);
                    plugin.saveLojasConfig();

                    // Quebrar placa da loja
                    String[] coords = locKey.split(",");
                    World world = Bukkit.getWorld(coords[0]);
                    int x = Integer.parseInt(coords[1]);
                    int y = Integer.parseInt(coords[2]);
                    int z = Integer.parseInt(coords[3]);

                    Location placaLoc = new Location(world, x, y, z);
                    if (placaLoc.getBlock().getState() instanceof Sign) {
                        placaLoc.getBlock().breakNaturally();
                    }

                    if (isServidorLoja) {
                        player.sendMessage(ChatColor.GREEN + "Loja do SERVIDOR removida com sucesso!");
                        plugin.getLogger().info("[MinhaLoja] " + player.getName() + " removeu uma loja do SERVIDOR em " + locKey);
                    } else {
                        player.sendMessage(ChatColor.GREEN + "Loja removida com sucesso!");
                    }
                    return;
                }
            }
        }
    }
}