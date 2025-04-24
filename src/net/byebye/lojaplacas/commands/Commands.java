package net.byebye.lojaplacas.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {

    private final List<String> subCommands = Arrays.asList("ajuda", "info");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser executado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("minhaloja.usar")) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando!");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("ajuda")) {
            enviarAjuda(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            enviarInfo(player);
            return true;
        }

        // Comando desconhecido
        player.sendMessage(ChatColor.RED + "Comando desconhecido! Use /minhaloja ajuda para ver os comandos disponíveis.");
        return true;
    }

    private void enviarAjuda(Player player) {
        player.sendMessage(ChatColor.GOLD + "=====[ " + ChatColor.YELLOW + "MinhaLoja" + ChatColor.GOLD + " ]=====");
        player.sendMessage(ChatColor.YELLOW + "/minhaloja ajuda " + ChatColor.GRAY + "- Mostra esta mensagem de ajuda");
        player.sendMessage(ChatColor.YELLOW + "/minhaloja info " + ChatColor.GRAY + "- Mostra informações sobre o plugin");
        player.sendMessage(ChatColor.GOLD + "Como criar uma loja:");
        player.sendMessage(ChatColor.GRAY + "1. Coloque uma placa");
        player.sendMessage(ChatColor.GRAY + "2. Escreva \"minhaloja\" na primeira linha");
        player.sendMessage(ChatColor.GRAY + "3. Configure sua loja no menu que aparecer");
    }

    private void enviarInfo(Player player) {
        player.sendMessage(ChatColor.GOLD + "=====[ " + ChatColor.YELLOW + "MinhaLoja" + ChatColor.GOLD + " ]=====");
        player.sendMessage(ChatColor.GRAY + "Plugin que permite criar lojas de compra e venda de itens via placas");
        player.sendMessage(ChatColor.GRAY + "Versão: " + ChatColor.YELLOW + "1.0");
        player.sendMessage(ChatColor.GRAY + "Desenvolvido para: " + ChatColor.YELLOW + "Minecraft Java 1.21.4");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}