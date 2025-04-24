package net.byebye.lojaplacas.utils;

import net.byebye.lojaplacas.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaginaLoja {

    private final Main plugin = Main.getInstance();
    private final int ITENS_POR_PAGINA = 45;

    public void mostrarPaginaLojas(Player player, int pagina) {
        ConfigurationSection lojasSection = plugin.getLojasConfig().getConfigurationSection("lojas");

        if (lojasSection == null || lojasSection.getKeys(false).isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Não há lojas cadastradas!");
            return;
        }

        List<String> lojaIds = new ArrayList<>(lojasSection.getKeys(false));
        int totalLojas = lojaIds.size();
        int totalPaginas = (int) Math.ceil((double) totalLojas / ITENS_POR_PAGINA);

        if (pagina < 1) {
            pagina = 1;
        } else if (pagina > totalPaginas) {
            pagina = totalPaginas;
        }

        int inicio = (pagina - 1) * ITENS_POR_PAGINA;
        int fim = Math.min(inicio + ITENS_POR_PAGINA, totalLojas);

        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_BLUE + "Lojas - Página " + pagina + "/" + totalPaginas);

        for (int i = inicio; i < fim; i++) {
            String locKey = lojaIds.get(i);
            ConfigurationSection lojaSection = lojasSection.getConfigurationSection(locKey);

            UUID donoUUID = UUID.fromString(lojaSection.getString("dono"));
            String donoNome = Bukkit.getOfflinePlayer(donoUUID).getName();
            ItemStack item = (ItemStack) lojaSection.get("item");
            double preco = lojaSection.getDouble("preco");
            int quantidade = lojaSection.getInt("quantidade");
            String tipo = lojaSection.getString("tipo");

            // Criar ItemStack representando a loja
            ItemStack lojaItem;
            if (item != null && item.getType() != Material.AIR) {
                lojaItem = item.clone();
            } else {
                lojaItem = new ItemStack(Material.BARRIER);
            }

            ItemMeta meta = lojaItem.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Loja de " + (donoNome != null ? donoNome : donoUUID));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Local: " + locKey);
            lore.add(ChatColor.GRAY + "Item: " + item.getType().name());
            lore.add(ChatColor.GRAY + "Preço: " + preco);
            lore.add(ChatColor.GRAY + "Quantidade: " + quantidade);
            lore.add(ChatColor.GRAY + "Tipo: " + tipo);
            lore.add("");
            lore.add(ChatColor.YELLOW + "Clique para teleportar");

            meta.setLore(lore);
            lojaItem.setItemMeta(meta);

            inv.setItem(i - inicio, lojaItem);
        }

        // Adicionar botões de navegação
        if (pagina > 1) {
            // Botão página anterior
            ItemStack anterior = new ItemStack(Material.ARROW);
            ItemMeta metaAnterior = anterior.getItemMeta();
            metaAnterior.setDisplayName(ChatColor.YELLOW + "Página Anterior");
            anterior.setItemMeta(metaAnterior);
            inv.setItem(45, anterior);
        }

        if (pagina < totalPaginas) {
            // Botão próxima página
            ItemStack proxima = new ItemStack(Material.ARROW);
            ItemMeta metaProxima = proxima.getItemMeta();
            metaProxima.setDisplayName(ChatColor.YELLOW + "Próxima Página");
            proxima.setItemMeta(metaProxima);
            inv.setItem(53, proxima);
        }

        // Voltar para o menu principal
        ItemStack voltar = new ItemStack(Material.BARRIER);
        ItemMeta metaVoltar = voltar.getItemMeta();
        metaVoltar.setDisplayName(ChatColor.RED + "Voltar");
        voltar.setItemMeta(metaVoltar);
        inv.setItem(49, voltar);

        player.openInventory(inv);
    }

    public void mostrarMinhasLojas(Player player, int pagina) {
        ConfigurationSection lojasSection = plugin.getLojasConfig().getConfigurationSection("lojas");

        if (lojasSection == null || lojasSection.getKeys(false).isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Você não tem lojas cadastradas!");
            return;
        }

        List<String> minhasLojas = new ArrayList<>();

        for (String locKey : lojasSection.getKeys(false)) {
            ConfigurationSection lojaSection = lojasSection.getConfigurationSection(locKey);
            UUID donoUUID = UUID.fromString(lojaSection.getString("dono"));

            if (donoUUID.equals(player.getUniqueId())) {
                minhasLojas.add(locKey);
            }
        }

        if (minhasLojas.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Você não tem lojas cadastradas!");
            return;
        }

        int totalLojas = minhasLojas.size();
        int totalPaginas = (int) Math.ceil((double) totalLojas / ITENS_POR_PAGINA);

        if (pagina < 1) {
            pagina = 1;
        } else if (pagina > totalPaginas) {
            pagina = totalPaginas;
        }

        int inicio = (pagina - 1) * ITENS_POR_PAGINA;
        int fim = Math.min(inicio + ITENS_POR_PAGINA, totalLojas);

        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Minhas Lojas - Página " + pagina + "/" + totalPaginas);

        for (int i = inicio; i < fim; i++) {
            String locKey = minhasLojas.get(i);
            ConfigurationSection lojaSection = lojasSection.getConfigurationSection(locKey);

            ItemStack item = (ItemStack) lojaSection.get("item");
            double preco = lojaSection.getDouble("preco");
            int quantidade = lojaSection.getInt("quantidade");
            String tipo = lojaSection.getString("tipo");

            // Criar ItemStack representando a loja
            ItemStack lojaItem;
            if (item != null && item.getType() != Material.AIR) {
                lojaItem = item.clone();
            } else {
                lojaItem = new ItemStack(Material.BARRIER);
            }

            ItemMeta meta = lojaItem.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Sua Loja");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Local: " + locKey);
            lore.add(ChatColor.GRAY + "Item: " + item.getType().name());
            lore.add(ChatColor.GRAY + "Preço: " + preco);
            lore.add(ChatColor.GRAY + "Quantidade: " + quantidade);
            lore.add(ChatColor.GRAY + "Tipo: " + tipo);
            lore.add("");
            lore.add(ChatColor.YELLOW + "Clique para editar");
            lore.add(ChatColor.RED + "Clique com shift para remover");

            meta.setLore(lore);
            lojaItem.setItemMeta(meta);

            inv.setItem(i - inicio, lojaItem);
        }

        // Adicionar botões de navegação
        if (pagina > 1) {
            // Botão página anterior
            ItemStack anterior = new ItemStack(Material.ARROW);
            ItemMeta metaAnterior = anterior.getItemMeta();
            metaAnterior.setDisplayName(ChatColor.YELLOW + "Página Anterior");
            anterior.setItemMeta(metaAnterior);
            inv.setItem(45, anterior);
        }

        if (pagina < totalPaginas) {
            // Botão próxima página
            ItemStack proxima = new ItemStack(Material.ARROW);
            ItemMeta metaProxima = proxima.getItemMeta();
            metaProxima.setDisplayName(ChatColor.YELLOW + "Próxima Página");
            proxima.setItemMeta(metaProxima);
            inv.setItem(53, proxima);
        }

        // Voltar para o menu principal
        ItemStack voltar = new ItemStack(Material.BARRIER);
        ItemMeta metaVoltar = voltar.getItemMeta();
        metaVoltar.setDisplayName(ChatColor.RED + "Voltar");
        voltar.setItemMeta(metaVoltar);
        inv.setItem(49, voltar);

        player.openInventory(inv);
    }
}
