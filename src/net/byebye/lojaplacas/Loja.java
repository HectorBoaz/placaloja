package net.byebye.lojaplacas;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Loja implements ConfigurationSerializable {
    private UUID dono;
    private Location placaLocation;
    private ItemStack item;
    private double preco;
    private int quantidade;
    private LojaTemp.TipoLoja tipo;
    private Location chestLocation;

    public Loja(UUID dono, Location placaLocation, ItemStack item, double preco, int quantidade, LojaTemp.TipoLoja tipo, Location chestLocation) {
        this.dono = dono;
        this.placaLocation = placaLocation;
        this.item = item;
        this.preco = preco;
        this.quantidade = quantidade;
        this.tipo = tipo;
        this.chestLocation = chestLocation;
    }

    public Loja(Map<String, Object> map) {
        this.dono = UUID.fromString((String) map.get("dono"));

        // Deserializar localização da placa
        String worldName = (String) map.get("world");
        double x = (double) map.get("x");
        double y = (double) map.get("y");
        double z = (double) map.get("z");
        this.placaLocation = new Location(Bukkit.getWorld(worldName), x, y, z);

        // Deserializar localização do baú
        String chestWorldName = (String) map.get("chestWorld");
        double chestX = (double) map.get("chestX");
        double chestY = (double) map.get("chestY");
        double chestZ = (double) map.get("chestZ");
        this.chestLocation = new Location(Bukkit.getWorld(chestWorldName), chestX, chestY, chestZ);

        this.item = (ItemStack) map.get("item");
        this.preco = (double) map.get("preco");
        this.quantidade = (int) map.get("quantidade");
        this.tipo = LojaTemp.TipoLoja.valueOf((String) map.get("tipo"));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("dono", dono.toString());

        // Serializar localização da placa
        map.put("world", placaLocation.getWorld().getName());
        map.put("x", placaLocation.getX());
        map.put("y", placaLocation.getY());
        map.put("z", placaLocation.getZ());

        // Serializar localização do baú
        map.put("chestWorld", chestLocation.getWorld().getName());
        map.put("chestX", chestLocation.getX());
        map.put("chestY", chestLocation.getY());
        map.put("chestZ", chestLocation.getZ());

        map.put("item", item);
        map.put("preco", preco);
        map.put("quantidade", quantidade);
        map.put("tipo", tipo.name());

        return map;
    }

    public UUID getDono() {
        return dono;
    }

    public Location getPlacaLocation() {
        return placaLocation;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPreco() {
        return preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public LojaTemp.TipoLoja getTipo() {
        return tipo;
    }

    public Location getChestLocation() {
        return chestLocation;
    }

    public Chest getBau() {
        if (chestLocation == null) return null;

        Block block = chestLocation.getBlock();
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            return (Chest) block.getState();
        }

        return null;
    }

    public boolean temEstoque() {
        Chest chest = getBau();
        if (chest == null) return false;

        Inventory inv = chest.getInventory();
        int quantidadeEncontrada = 0;

        for (ItemStack itemInv : inv.getContents()) {
            if (itemInv != null && itemInv.isSimilar(item)) {
                quantidadeEncontrada += itemInv.getAmount();

                if (quantidadeEncontrada >= quantidade) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean temEspacoNoEstoque() {
        Chest chest = getBau();
        if (chest == null) return false;

        Inventory inv = chest.getInventory();
        ItemStack copia = item.clone();
        copia.setAmount(quantidade);

        // Verificar se cabe no baú
        int espacosLivres = 0;

        for (ItemStack itemInv : inv.getContents()) {
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

    public void removerItemDoEstoque() {
        Chest chest = getBau();
        if (chest == null) return;

        Inventory inv = chest.getInventory();
        int quantidadeRestante = quantidade;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack itemInv = inv.getItem(i);

            if (itemInv != null && itemInv.isSimilar(item)) {
                int qtdItem = itemInv.getAmount();

                if (qtdItem <= quantidadeRestante) {
                    // Remover todo o stack
                    inv.setItem(i, null);
                    quantidadeRestante -= qtdItem;
                } else {
                    // Remover parte do stack
                    itemInv.setAmount(qtdItem - quantidadeRestante);
                    quantidadeRestante = 0;
                }

                if (quantidadeRestante == 0) {
                    break;
                }
            }
        }

        chest.update();
    }

    public void adicionarItemAoEstoque(ItemStack itemToAdd) {
        Chest chest = getBau();
        if (chest == null) return;

        Inventory inv = chest.getInventory();
        inv.addItem(itemToAdd);

        chest.update();
    }
}