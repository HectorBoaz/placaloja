package net.byebye.lojaplacas.gui;

import net.byebye.lojaplacas.LojaTemp;
import net.byebye.lojaplacas.Main;
import net.byebye.lojaplacas.ServidorLoja;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LojaGUI {

    public static final String TITULO_CONFIG = ChatColor.DARK_BLUE + "Configuração da Loja";
    public static final String TITULO_CONFIG_SERVIDOR = ChatColor.DARK_RED + "Loja do SERVIDOR";
    public static final String TITULO_SELECAO_ITEM = ChatColor.DARK_GREEN + "Selecione o Item";

    public static final int SLOT_ITEM = 11;
    public static final int SLOT_PRECO = 13;
    public static final int SLOT_QUANTIDADE = 15;
    public static final int SLOT_TIPO = 22;
    public static final int SLOT_CONFIRMAR = 30;
    public static final int SLOT_CANCELAR = 32;

    public static void abrirConfiguracaoGUI(Player player) {
        // Verificar se é modo desenvolvedor
        Main plugin = Main.getInstance();
        boolean isServidorLoja = plugin.isModoDesenvolvedor() && player.hasPermission("minhaloja.admin");

        // Verificar se existe uma loja temporária para este jogador
        LojaTemp lojaTemp = plugin.getLojaTemp(player.getUniqueId());
        if (lojaTemp == null) {
            plugin.getLogger().warning("[MinhaLoja] Tentativa de abrir GUI para jogador " + player.getName() + " mas não há loja temporária registrada.");
            player.sendMessage(ChatColor.RED + "Erro ao abrir menu de configuração. Tente clicar na placa novamente.");
            return;
        }

        plugin.getLogger().info("[MinhaLoja] Abrindo GUI para jogador " + player.getName() + (isServidorLoja ? " (MODO SERVIDOR)" : ""));

        // Título deve ter no máximo 32 caracteres devido a limitações do Bukkit
        Inventory inv = Bukkit.createInventory(null, 45, isServidorLoja ? TITULO_CONFIG_SERVIDOR : TITULO_CONFIG);

        // Obter loja temporária
        if (lojaTemp == null) return;

        // Item para venda
        ItemStack itemVenda;
        if (lojaTemp.getItem() != null && lojaTemp.getItem().getType() != Material.AIR) {
            itemVenda = lojaTemp.getItem().clone();
            ItemMeta meta = itemVenda.getItemMeta();
            List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.YELLOW + "Clique para alterar o item");
            meta.setLore(lore);
            itemVenda.setItemMeta(meta);
        } else {
            itemVenda = new ItemStack(Material.BARRIER);
            ItemMeta meta = itemVenda.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Selecione um Item");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Clique para selecionar o item para venda");
            meta.setLore(lore);
            itemVenda.setItemMeta(meta);
        }
        inv.setItem(SLOT_ITEM, itemVenda);

        // Preço
        ItemStack itemPreco = new ItemStack(Material.GOLD_INGOT);
        ItemMeta metaPreco = itemPreco.getItemMeta();
        metaPreco.setDisplayName(ChatColor.YELLOW + "Preço: " + lojaTemp.getPreco());
        List<String> lorePreco = new ArrayList<>();
        lorePreco.add(ChatColor.GRAY + "Preço atual: " + lojaTemp.getPreco());
        lorePreco.add("");
        lorePreco.add(ChatColor.YELLOW + "Clique para definir o preço");
        metaPreco.setLore(lorePreco);
        itemPreco.setItemMeta(metaPreco);
        inv.setItem(SLOT_PRECO, itemPreco);

        // Quantidade
        ItemStack itemQtd = new ItemStack(Material.HOPPER);
        ItemMeta metaQtd = itemQtd.getItemMeta();
        metaQtd.setDisplayName(ChatColor.AQUA + "Quantidade: " + lojaTemp.getQuantidade());
        List<String> loreQtd = new ArrayList<>();
        loreQtd.add(ChatColor.GRAY + "Quantidade atual: " + lojaTemp.getQuantidade());
        loreQtd.add("");
        loreQtd.add(ChatColor.YELLOW + "Clique para definir a quantidade");
        metaQtd.setLore(loreQtd);
        itemQtd.setItemMeta(metaQtd);
        inv.setItem(SLOT_QUANTIDADE, itemQtd);

        // Tipo de loja
        ItemStack itemTipo;
        String tipoNome;
        List<String> loreTipo = new ArrayList<>();

        if (lojaTemp.getTipo() == LojaTemp.TipoLoja.VENDA) {
            itemTipo = new ItemStack(Material.CHEST);
            tipoNome = ChatColor.GREEN + "Tipo: Venda";
            loreTipo.add(ChatColor.GRAY + "Modo atual: " + ChatColor.GREEN + "VENDA");
            loreTipo.add(ChatColor.GRAY + "Você venderá itens para os jogadores");
            loreTipo.add(ChatColor.GRAY + "Os itens serão retirados do baú");
        } else {
            itemTipo = new ItemStack(Material.HOPPER);
            tipoNome = ChatColor.BLUE + "Tipo: Compra";
            loreTipo.add(ChatColor.GRAY + "Modo atual: " + ChatColor.BLUE + "COMPRA");
            loreTipo.add(ChatColor.GRAY + "Você comprará itens dos jogadores");
            loreTipo.add(ChatColor.GRAY + "Os itens serão armazenados no baú");
        }

        loreTipo.add("");
        loreTipo.add(ChatColor.YELLOW + "Clique para alternar o tipo");

        ItemMeta metaTipo = itemTipo.getItemMeta();
        metaTipo.setDisplayName(tipoNome);
        metaTipo.setLore(loreTipo);
        itemTipo.setItemMeta(metaTipo);
        inv.setItem(SLOT_TIPO, itemTipo);

        // Informação do Baú
        ItemStack itemBau;
        if (lojaTemp.temBauAdjacente()) {
            itemBau = new ItemStack(Material.CHEST);
            ItemMeta metaBau = itemBau.getItemMeta();
            metaBau.setDisplayName(ChatColor.GREEN + "Baú Conectado");

            List<String> loreBau = new ArrayList<>();
            loreBau.add(ChatColor.GRAY + "Sua loja está conectada a um baú");

            // Verificar estoque atual se for loja de venda
            if (lojaTemp.getTipo() == LojaTemp.TipoLoja.VENDA && lojaTemp.getItem() != null && lojaTemp.getItem().getType() != Material.AIR) {
                Chest chest = lojaTemp.getBau();
                if (chest != null) {
                    int estoqueAtual = 0;

                    for (ItemStack item : chest.getInventory().getContents()) {
                        if (item != null && item.isSimilar(lojaTemp.getItem())) {
                            estoqueAtual += item.getAmount();
                        }
                    }

                    if (estoqueAtual >= lojaTemp.getQuantidade()) {
                        loreBau.add(ChatColor.GREEN + "Estoque: " + estoqueAtual + " itens");
                    } else {
                        loreBau.add(ChatColor.RED + "Estoque insuficiente!");
                        loreBau.add(ChatColor.RED + "Disponível: " + estoqueAtual + "/" + lojaTemp.getQuantidade());
                    }
                }
            }

            metaBau.setLore(loreBau);
            itemBau.setItemMeta(metaBau);
        } else {
            itemBau = new ItemStack(Material.BARRIER);
            ItemMeta metaBau = itemBau.getItemMeta();
            metaBau.setDisplayName(ChatColor.RED + "Baú Não Encontrado");

            List<String> loreBau = new ArrayList<>();
            loreBau.add(ChatColor.RED + "Você precisa colocar a placa");
            loreBau.add(ChatColor.RED + "adjacente a um baú!");

            metaBau.setLore(loreBau);
            itemBau.setItemMeta(metaBau);
        }
        inv.setItem(4, itemBau);

        // Modo desenvolvedor - informação
        if (isServidorLoja) {
            ItemStack itemInfo = new ItemStack(Material.REDSTONE_TORCH);
            ItemMeta metaInfo = itemInfo.getItemMeta();
            metaInfo.setDisplayName(ChatColor.RED + "MODO DESENVOLVEDOR");
            List<String> loreInfo = new ArrayList<>();
            loreInfo.add(ChatColor.YELLOW + "Esta loja pertencerá ao SERVIDOR");
            loreInfo.add(ChatColor.YELLOW + "Os jogadores poderão comprar/vender");
            loreInfo.add(ChatColor.YELLOW + "sem que o dinheiro vá para ninguém");
            metaInfo.setLore(loreInfo);
            itemInfo.setItemMeta(metaInfo);
            inv.setItem(49, itemInfo);
        }

        // Botão confirmar
        ItemStack itemConfirmar = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta metaConfirmar = itemConfirmar.getItemMeta();
        metaConfirmar.setDisplayName(ChatColor.GREEN + "Confirmar");
        List<String> loreConfirmar = new ArrayList<>();

        boolean podeConfirmar = lojaTemp.isConfigCompleta();

        // Verificar estoque para lojas de venda
        if (podeConfirmar && lojaTemp.getTipo() == LojaTemp.TipoLoja.VENDA) {
            // No modo desenvolvedor, não é necessário verificar estoque

            if (!isServidorLoja) {
                Chest chest = lojaTemp.getBau();
                if (chest != null) {
                    int estoqueAtual = 0;

                    for (ItemStack item : chest.getInventory().getContents()) {
                        if (item != null && item.isSimilar(lojaTemp.getItem())) {
                            estoqueAtual += item.getAmount();
                        }
                    }

                    if (estoqueAtual < lojaTemp.getQuantidade()) {
                        podeConfirmar = false;
                        loreConfirmar.add(ChatColor.RED + "Estoque insuficiente no baú!");
                        loreConfirmar.add(ChatColor.RED + "Disponível: " + estoqueAtual + "/" + lojaTemp.getQuantidade());
                    }
                }
            } else {
                // Lojas do servidor são de estoque infinito
                loreConfirmar.add(ChatColor.GREEN + "Modo SERVIDOR: Estoque infinito");
            }
        }

        if (podeConfirmar) {
            loreConfirmar.add(ChatColor.GRAY + "Clique para confirmar a configuração");
            loreConfirmar.add(ChatColor.GRAY + "e ativar sua loja");
        } else if (!lojaTemp.temBauAdjacente()) {
            loreConfirmar.add(ChatColor.RED + "Você precisa colocar a placa");
            loreConfirmar.add(ChatColor.RED + "adjacente a um baú!");
        } else {
            loreConfirmar.add(ChatColor.RED + "Você precisa configurar todos");
            loreConfirmar.add(ChatColor.RED + "os aspectos da loja antes");
            loreConfirmar.add(ChatColor.RED + "de confirmar!");
        }

        metaConfirmar.setLore(loreConfirmar);
        itemConfirmar.setItemMeta(metaConfirmar);
        inv.setItem(SLOT_CONFIRMAR, itemConfirmar);

        // Botão cancelar
        ItemStack itemCancelar = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta metaCancelar = itemCancelar.getItemMeta();
        metaCancelar.setDisplayName(ChatColor.RED + "Cancelar");
        List<String> loreCancelar = new ArrayList<>();
        loreCancelar.add(ChatColor.GRAY + "Clique para cancelar a");
        loreCancelar.add(ChatColor.GRAY + "configuração e remover a loja");
        metaCancelar.setLore(loreCancelar);
        itemCancelar.setItemMeta(metaCancelar);
        inv.setItem(SLOT_CANCELAR, itemCancelar);

        // Decoração
        ItemStack vidro = new ItemStack(
                isServidorLoja ? Material.RED_STAINED_GLASS_PANE : Material.BLACK_STAINED_GLASS_PANE,
                1);
        ItemMeta metaVidro = vidro.getItemMeta();
        metaVidro.setDisplayName(" ");
        vidro.setItemMeta(metaVidro);

        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, vidro);
            }
        }

        player.openInventory(inv);
    }

    public static void abrirSelecaoItemGUI(Player player) {
        // Título deve ter no máximo 32 caracteres devido a limitações do Bukkit
        Inventory inv = Bukkit.createInventory(null, 54, TITULO_SELECAO_ITEM);

        // Adicionar instrução visual
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GOLD + "Selecione um Item");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.YELLOW + "Clique em um item do seu inventário");
        infoLore.add(ChatColor.YELLOW + "para selecioná-lo para a loja");
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);

        inv.setItem(4, info);

        // Adicionar botões de vidro para decoração
        ItemStack vidro = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta metaVidro = vidro.getItemMeta();
        metaVidro.setDisplayName(" ");
        vidro.setItemMeta(metaVidro);

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, vidro);
        }

        player.openInventory(inv);
    }
}