package net.byebye.lojaplacas;

import net.byebye.lojaplacas.commands.Commands;
import net.byebye.lojaplacas.commands.CommandsAdmin;
import net.byebye.lojaplacas.events.ChatListener;
import net.byebye.lojaplacas.events.Events;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin {

    private static Main instance;
    private File lojaFile;
    private FileConfiguration lojaConfig;
    private final Map<UUID, LojaTemp> lojasTemp = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        // Salvar config padrão
        saveDefaultConfig();

        // Inicializar arquivo de lojas
        setupLojasFile();

        // Registrar eventos
        Bukkit.getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);

        // Registrar comandos
        getCommand("minhaloja").setExecutor(new Commands());
        getCommand("minhaloja").setTabCompleter(new Commands());
        getCommand("minhaloadmin").setExecutor(new CommandsAdmin());
        getCommand("minhaloadmin").setTabCompleter(new CommandsAdmin());

        getLogger().info("Plugin MinhaLoja ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        saveLojasConfig();
        getLogger().info("Plugin MinhaLoja desativado com sucesso!");
    }

    private void setupLojasFile() {
        lojaFile = new File(getDataFolder(), "lojas.yml");

        if (!lojaFile.exists()) {
            try {
                lojaFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Não foi possível criar o arquivo lojas.yml");
                e.printStackTrace();
            }
        }

        lojaConfig = YamlConfiguration.loadConfiguration(lojaFile);
    }

    public FileConfiguration getLojasConfig() {
        return lojaConfig;
    }

    public void saveLojasConfig() {
        try {
            lojaConfig.save(lojaFile);
        } catch (IOException e) {
            getLogger().severe("Não foi possível salvar o arquivo lojas.yml");
            e.printStackTrace();
        }
    }

    public Map<UUID, LojaTemp> getLojasTemp() {
        return lojasTemp;
    }

    public void addLojaTemp(UUID uuid, LojaTemp lojaTemp) {
        lojasTemp.put(uuid, lojaTemp);
    }

    public void removeLojaTemp(UUID uuid) {
        lojasTemp.remove(uuid);
    }

    public LojaTemp getLojaTemp(UUID uuid) {
        return lojasTemp.get(uuid);
    }

    public static Main getInstance() {
        return instance;
    }
}