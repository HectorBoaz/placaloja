package net.byebye.lojaplacas.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    /**
     * Formata uma mensagem adicionando cores
     *
     * @param mensagem Mensagem a ser formatada
     * @return Mensagem formatada
     */
    public static String formatarMensagem(String mensagem) {
        return ChatColor.translateAlternateColorCodes('&', mensagem);
    }

    /**
     * Obtém uma mensagem da configuração
     *
     * @param config Configuração
     * @param caminho Caminho da mensagem
     * @return Mensagem formatada
     */
    public static String obterMensagem(FileConfiguration config, String caminho) {
        String prefixo = formatarMensagem(config.getString("mensagens.prefixo", "&6[MinhaLoja] &r"));
        String mensagem = config.getString(caminho, "");

        return prefixo + formatarMensagem(mensagem);
    }

    /**
     * Cria um ItemStack com nome e descrição personalizados
     *
     * @param material Material do item
     * @param nome Nome do item
     * @param lore Descrição do item
     * @return ItemStack criado
     */
    public static ItemStack criarItem(org.bukkit.Material material, String nome, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (nome != null) {
            meta.setDisplayName(formatarMensagem(nome));
        }

        if (lore != null) {
            List<String> loreFormatada = new ArrayList<>();
            for (String linha : lore) {
                loreFormatada.add(formatarMensagem(linha));
            }
            meta.setLore(loreFormatada);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Verifica se um jogador tem espaço suficiente no inventário
     *
     //* @param player Jogador
     //* @param item Item a ser verificado
     //* @param quantidade Quantidade do item
     * @return true se tem espaço, false caso contrário
     */

    public boolean inventarioContem(Inventory inv, ItemStack itemProcurado, int quantidade) {
        int quantidadeTotal = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.isSimilar(itemProcurado)) {
                quantidadeTotal += item.getAmount();
                if (quantidadeTotal >= quantidade) return true;
            }
        }
        return false;
    }

    public void removerItensDoInventario(Inventory inv, ItemStack itemParaRemover, int quantidade) {
        int restante = quantidade;
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.isSimilar(itemParaRemover)) {
                int quantidadeItem = item.getAmount();
                if (quantidadeItem <= restante) {
                    inv.setItem(i, null);
                    restante -= quantidadeItem;
                } else {
                    item.setAmount(quantidadeItem - restante);
                    restante = 0;
                }
                if (restante == 0) break;
            }
        }
    }

    public boolean temEspacoPara(Inventory inv, ItemStack item, int quantidade) {
        int espacos = 0;
        for (ItemStack slot : inv.getStorageContents()) {
            if (slot == null || slot.getType() == Material.AIR) {
                espacos += item.getMaxStackSize();
            } else if (slot.isSimilar(item)) {
                espacos += (item.getMaxStackSize() - slot.getAmount());
            }

            if (espacos >= quantidade) return true;
        }
        return false;
    }

    public static boolean temEspacoNoInventario(Player player, ItemStack item, int quantidade) {
        ItemStack copia = item.clone();
        copia.setAmount(quantidade);

        // Verificar se cabe no inventário
        int espacosLivres = 0;

        for (ItemStack itemInv : player.getInventory().getStorageContents()) {
            if (itemInv == null) {
                espacosLivres += item.getMaxStackSize();
                continue;
            }

            if (itemInv.isSimilar(item)) {
                espacosLivres += (item.getMaxStackSize() - itemInv.getAmount());
            }
        }

        return espacosLivres >= quantidade;
    }

    /**
     * Formata um preço com duas casas decimais
     *
     * @param valor Valor a ser formatado
     * @return Valor formatado
     */
    public static String formatarPreco(double valor) {
        return String.format("%.2f", valor);
    }
}