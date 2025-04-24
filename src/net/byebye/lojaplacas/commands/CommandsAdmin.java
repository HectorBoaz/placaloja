package net.byebye.lojaplacas.commands;

import net.byebye.lojaplacas.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandsAdmin implements CommandExecutor, TabCompleter {

    private final Main plugin = Main.getInstance();
    private final List<String> subCommands = Arrays.asList("ajuda", "list", "reload", "remove");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("minhaloja.admin")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando!");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("ajuda")) {
            enviarAjuda(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            listarLojas(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            recarregarPlugin(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Uso: /minhaloadmin remove <id>");
                return true;
            }

            removerLoja(sender, args[1]);
            return true;
        }

        // Comando desconhecido
        sender.sendMessage(ChatColor.RED + "Comando desconhecido! Use /minhaloadmin ajuda para ver os comandos disponíveis.");
        return true;
    }

    private void enviarAjuda(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=====[ " + ChatColor.YELLOW + "MinhaLoja Admin" + ChatColor.GOLD + " ]=====");
        sender.sendMessage(ChatColor.YELLOW + "/minhaloadmin ajuda " + ChatColor.GRAY + "- Mostra esta mensagem de ajuda");
        sender.sendMessage(ChatColor.YELLOW + "/minhaloadmin list " + ChatColor.GRAY + "- Lista todas as lojas");
        sender.sendMessage(ChatColor.YELLOW + "/minhaloadmin reload " + ChatColor.GRAY + "- Recarrega a configuração do plugin");
        sender.sendMessage(ChatColor.YELLOW + "/minhaloadmin remove <id> " + ChatColor.GRAY + "- Remove uma loja pelo ID");
    }

    private void listarLojas(CommandSender sender) {
        ConfigurationSection lojasSection = plugin.getLojasConfig().getConfigurationSection("lojas");

        if (lojasSection == null || lojasSection.getKeys(false).isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Não há lojas cadastradas!");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=====[ " + ChatColor.YELLOW + "Lista de Lojas" + ChatColor.GOLD + " ]=====");

        int i = 1;
        for (String locKey : lojasSection.getKeys(false)) {
            ConfigurationSection lojaSection = lojasSection.getConfigurationSection(locKey);

            String donoUUID = lojaSection.getString("dono");
            String donoNome = Bukkit.getOfflinePlayer(java.util.UUID.fromString(donoUUID)).getName();
            String itemNome = lojaSection.getString("item.type", "Desconhecido");
            double preco = lojaSection.getDouble("preco");
            int quantidade = lojaSection.getInt("quantidade");
            String tipo = lojaSection.getString("tipo");

            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + i + ChatColor.GRAY + "] " +
                    ChatColor.GOLD + "ID: " + ChatColor.YELLOW + locKey + ChatColor.GRAY + " | " +
                    ChatColor.GOLD + "Dono: " + ChatColor.YELLOW + (donoNome != null ? donoNome : donoUUID) + ChatColor.GRAY + " | " +
                    ChatColor.GOLD + "Item: " + ChatColor.YELLOW + itemNome + ChatColor.GRAY + " | " +
                    ChatColor.GOLD + "Preço: " + ChatColor.YELLOW + preco + ChatColor.GRAY + " | " +
                    ChatColor.GOLD + "Qtd: " + ChatColor.YELLOW + quantidade + ChatColor.GRAY + " | " +
                    ChatColor.GOLD + "Tipo: " + ChatColor.YELLOW + tipo);

            i++;
        }
    }

    private void recarregarPlugin(CommandSender sender) {
        plugin.reloadConfig();

        // Recarregar arquivo de lojas
        plugin.saveLojasConfig();

        sender.sendMessage(ChatColor.GREEN + "Plugin recarregado com sucesso!");
    }

    private void removerLoja(CommandSender sender, String locKey) {
        ConfigurationSection lojasSection = plugin.getLojasConfig().getConfigurationSection("lojas");

        if (lojasSection == null || !lojasSection.contains(locKey)) {
            sender.sendMessage(ChatColor.RED + "Loja com ID " + locKey + " não encontrada!");
            return;
        }

        // Remover placa física se possível
        try {
            String[] coordenadas = locKey.split(",");
            World world = Bukkit.getWorld(coordenadas[0]);
            double x = Double.parseDouble(coordenadas[1]);
            double y = Double.parseDouble(coordenadas[2]);
            double z = Double.parseDouble(coordenadas[3]);

            Location location = new Location(world, x, y, z);
            location.getBlock().breakNaturally();
        } catch (Exception e) {
            sender.sendMessage(ChatColor.YELLOW + "Não foi possível remover a placa física, mas a loja foi removida do banco de dados.");
        }

        // Remover loja da configuração
        plugin.getLojasConfig().set("lojas." + locKey, null);
        plugin.saveLojasConfig();

        sender.sendMessage(ChatColor.GREEN + "Loja removida com sucesso!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("minhaloja.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            ConfigurationSection lojasSection = plugin.getLojasConfig().getConfigurationSection("lojas");

            if (lojasSection != null) {
                Set<String> lojaIds = lojasSection.getKeys(false);

                return lojaIds.stream()
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}