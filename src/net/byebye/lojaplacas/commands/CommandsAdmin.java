package net.byebye.lojaplacas.commands;

import net.byebye.lojaplacas.Main;
import net.byebye.lojaplacas.ServidorLoja;
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
    private final List<String> subCommands = Arrays.asList("ajuda", "list", "reload", "remove", "dev", "servidor");

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

        if (args[0].equalsIgnoreCase("dev")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Uso: /minhaloadmin dev <on|off>");
                return true;
            }

            if (args[1].equalsIgnoreCase("on")) {
                plugin.ativarModoDesenvolvedor();
                sender.sendMessage(ChatColor.GREEN + "Modo desenvolvedor ativado! Agora você pode criar lojas do SERVIDOR.");
                sender.sendMessage(ChatColor.YELLOW + "No modo desenvolvedor, as lojas têm estoque infinito e não precisam de itens no baú.");
                return true;
            } else if (args[1].equalsIgnoreCase("off")) {
                plugin.desativarModoDesenvolvedor();
                sender.sendMessage(ChatColor.RED + "Modo desenvolvedor desativado!");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Uso: /minhaloadmin dev <on|off>");
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("servidor")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Uso: /minhaloadmin servidor <list|remove>");
                return true;
            }

            if (args[1].equalsIgnoreCase("list")) {
                listarLojasServidor(sender);
                return true;
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Uso: /minhaloadmin servidor remove <id>");
                    return true;
                }

                removerLojaServidor(sender, args[2]);
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Uso: /minhaloadmin servidor <list|remove>");
                return true;
            }
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
        sender.sendMessage(ChatColor.YELLOW + "/minhaloadmin dev <on|off> " + ChatColor.GRAY + "- Ativa/desativa o modo desenvolvedor");
        sender.sendMessage(ChatColor.YELLOW + "/minhaloadmin servidor list " + ChatColor.GRAY + "- Lista todas as lojas do servidor");
        sender.sendMessage(ChatColor.YELLOW + "/minhaloadmin servidor remove <id> " + ChatColor.GRAY + "- Remove uma loja do servidor pelo ID");
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
            String donoNome;

            if (donoUUID.equals(ServidorLoja.SERVIDOR_UUID.toString())) {
                donoNome = "SERVIDOR";
            } else {
                donoNome = Bukkit.getOfflinePlayer(java.util.UUID.fromString(donoUUID)).getName();
            }

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

    private void listarLojasServidor(CommandSender sender) {
        ConfigurationSection lojasSection = plugin.getLojasConfig().getConfigurationSection("lojas");

        if (lojasSection == null || lojasSection.getKeys(false).isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Não há lojas do servidor cadastradas!");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=====[ " + ChatColor.YELLOW + "Lista de Lojas do Servidor" + ChatColor.GOLD + " ]=====");

        int i = 1;
        int count = 0;
        for (String locKey : lojasSection.getKeys(false)) {
            ConfigurationSection lojaSection = lojasSection.getConfigurationSection(locKey);

            String donoUUID = lojaSection.getString("dono");

            // Verificar se é uma loja do servidor
            if (!donoUUID.equals(ServidorLoja.SERVIDOR_UUID.toString())) {
                continue;
            }

            count++;
            String itemNome = lojaSection.getString("item.type", "Desconhecido");
            double preco = lojaSection.getDouble("preco");
            int quantidade = lojaSection.getInt("quantidade");
            String tipo = lojaSection.getString("tipo");

            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + i + ChatColor.GRAY + "] " +
                    ChatColor.GOLD + "ID: " + ChatColor.YELLOW + locKey + ChatColor.GRAY + " | " +
                    ChatColor.GOLD + "Dono: " + ChatColor.YELLOW + "SERVIDOR" + ChatColor.GRAY + " | " +
                    ChatColor.GOLD + "Item: " + ChatColor.YELLOW + itemNome + ChatColor.GRAY + " | " +
                    ChatColor.GOLD + "Preço: " + ChatColor.YELLOW + preco + ChatColor.GRAY + " | " +
                    ChatColor.GOLD + "Qtd: " + ChatColor.YELLOW + quantidade + ChatColor.GRAY + " | " +
                    ChatColor.GOLD + "Tipo: " + ChatColor.YELLOW + tipo);

            i++;
        }

        if (count == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Não há lojas do servidor cadastradas!");
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

    private void removerLojaServidor(CommandSender sender, String locKey) {
        ConfigurationSection lojasSection = plugin.getLojasConfig().getConfigurationSection("lojas");

        if (lojasSection == null || !lojasSection.contains(locKey)) {
            sender.sendMessage(ChatColor.RED + "Loja do servidor com ID " + locKey + " não encontrada!");
            return;
        }

        // Verificar se é uma loja do servidor
        String donoUUID = lojasSection.getConfigurationSection(locKey).getString("dono");
        if (!donoUUID.equals(ServidorLoja.SERVIDOR_UUID.toString())) {
            sender.sendMessage(ChatColor.RED + "Esta loja não pertence ao servidor!");
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

        sender.sendMessage(ChatColor.GREEN + "Loja do servidor removida com sucesso!");
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

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                ConfigurationSection lojasSection = plugin.getLojasConfig().getConfigurationSection("lojas");

                if (lojasSection != null) {
                    Set<String> lojaIds = lojasSection.getKeys(false);

                    return lojaIds.stream()
                            .filter(s -> s.startsWith(args[1]))
                            .collect(Collectors.toList());
                }
            } else if (args[0].equalsIgnoreCase("dev")) {
                List<String> options = Arrays.asList("on", "off");
                return options.stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("servidor")) {
                List<String> options = Arrays.asList("list", "remove");
                return options.stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("servidor") && args[1].equalsIgnoreCase("remove")) {
                ConfigurationSection lojasSection = plugin.getLojasConfig().getConfigurationSection("lojas");

                if (lojasSection != null) {
                    List<String> lojaServidorIds = new ArrayList<>();

                    for (String locKey : lojasSection.getKeys(false)) {
                        String donoUUID = lojasSection.getConfigurationSection(locKey).getString("dono");
                        if (donoUUID.equals(ServidorLoja.SERVIDOR_UUID.toString())) {
                            lojaServidorIds.add(locKey);
                        }
                    }

                    return lojaServidorIds.stream()
                            .filter(s -> s.startsWith(args[2]))
                            .collect(Collectors.toList());
                }
            }
        }

        return new ArrayList<>();
    }
}