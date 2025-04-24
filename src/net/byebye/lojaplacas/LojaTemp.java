package net.byebye.lojaplacas;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public class LojaTemp {
    private Location placaLocation;
    private ItemStack item;
    private double preco;
    private int quantidade;
    private TipoLoja tipo;
    private Location chestLocation;

    public enum TipoLoja {
        VENDA,
        COMPRA
    }

    public LojaTemp(Location placaLocation) {
        this.placaLocation = placaLocation;
        this.item = new ItemStack(Material.AIR);
        this.preco = 0.0;
        this.quantidade = 1;
        this.tipo = TipoLoja.VENDA;
        this.chestLocation = buscarBauAdjacente(placaLocation);
    }

    public Location getPlacaLocation() {
        return placaLocation;
    }

    public void setPlacaLocation(Location placaLocation) {
        this.placaLocation = placaLocation;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public TipoLoja getTipo() {
        return tipo;
    }

    public void setTipo(TipoLoja tipo) {
        this.tipo = tipo;
    }

    public Location getChestLocation() {
        return chestLocation;
    }

    public void setChestLocation(Location chestLocation) {
        this.chestLocation = chestLocation;
    }

    public void alternarTipo() {
        if (tipo == TipoLoja.VENDA) {
            tipo = TipoLoja.COMPRA;
        } else {
            tipo = TipoLoja.VENDA;
        }
    }

    public boolean isConfigCompleta() {
        return item != null && !item.getType().equals(Material.AIR) && preco > 0 && quantidade > 0 && chestLocation != null;
    }

    public boolean temBauAdjacente() {
        return chestLocation != null;
    }

    private Location buscarBauAdjacente(Location location) {
        Block block = location.getBlock();

        // Verificar os seis lados do bloco
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

        for (BlockFace face : faces) {
            Block adjacente = block.getRelative(face);

            if (adjacente.getType() == Material.CHEST || adjacente.getType() == Material.TRAPPED_CHEST) {
                return adjacente.getLocation();
            }
        }

        return null;
    }

    public Chest getBau() {
        if (chestLocation == null) return null;

        Block block = chestLocation.getBlock();
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            return (Chest) block.getState();
        }

        return null;
    }
}