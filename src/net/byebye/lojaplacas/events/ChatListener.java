package net.byebye.lojaplacas.events;

import net.byebye.lojaplacas.LojaTemp;
import net.byebye.lojaplacas.Main;
import net.byebye.lojaplacas.gui.LojaGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {

    public static final Main plugin = Main.getInstance();
    public static final Map<UUID, ConfigType> aguardandoConfig = new HashMap<>();

    public enum ConfigType {
        PRECO,
        QUANTIDADE
    }

    public void aguardarConfiguracao(Player player, ConfigType tipo) {
        aguardandoConfig.put(player.getUniqueId(), tipo);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Verificar se o jogador está configurando uma loja
        if (!aguardandoConfig.containsKey(uuid)) return;

        event.setCancelled(true);

        ConfigType tipo = aguardandoConfig.remove(uuid);
        String mensagem = event.getMessage();

        // Processar configuração em uma task síncrona
        Bukkit.getScheduler().runTask(plugin, () -> {
            LojaTemp lojaTemp = plugin.getLojaTemp(uuid);

            if (lojaTemp == null) {
                player.sendMessage(ChatColor.RED + "Você não está configurando nenhuma loja!");
                return;
            }

            if (tipo == ConfigType.PRECO) {
                try {
                    double preco = Double.parseDouble(mensagem);

                    if (preco <= 0) {
                        player.sendMessage(ChatColor.RED + "O preço deve ser maior que zero!");
                        return;
                    }

                    lojaTemp.setPreco(preco);
                    player.sendMessage(ChatColor.GREEN + "Preço definido: " + preco);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Digite um número válido!");
                    return;
                }
            } else if (tipo == ConfigType.QUANTIDADE) {
                try {
                    int quantidade = Integer.parseInt(mensagem);

                    if (quantidade <= 0) {
                        player.sendMessage(ChatColor.RED + "A quantidade deve ser maior que zero!");
                        return;
                    }

                    lojaTemp.setQuantidade(quantidade);
                    player.sendMessage(ChatColor.GREEN + "Quantidade definida: " + quantidade);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Digite um número válido!");
                    return;
                }
            }

            // Reabrir GUI de configuração
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                LojaGUI.abrirConfiguracaoGUI(player);
            }, 10L);
        });
    }
}
