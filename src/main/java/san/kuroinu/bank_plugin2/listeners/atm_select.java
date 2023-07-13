package san.kuroinu.bank_plugin2.listeners;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import static san.kuroinu.bank_plugin2.Bank_Plugin2.atm_out_players;
import static san.kuroinu.bank_plugin2.Bank_Plugin2.atm_select_players;

public class atm_select implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if (atm_select_players.contains(event.getWhoClicked().getName())){
            event.setCancelled(true);
            Player e = (Player) event.getWhoClicked();
            if (event.getClickedInventory() == null){
                return;
            }
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null){
                return;
            }
            if (clickedItem.getType().equals(Material.HOPPER) && clickedItem.getItemMeta().getDisplayName().equals(ChatColor.BOLD+"お金を入れる")){
                atm_select_players.remove(e.getName());
                e.closeInventory();
                e.performCommand("atm in");
            }
            if (clickedItem.getType().equals(Material.CHEST) && clickedItem.getItemMeta().getDisplayName().equals(ChatColor.BOLD+"お金を出す")){
                atm_select_players.remove(e.getName());
                e.closeInventory();
                e.performCommand("atm out");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if (atm_select_players.contains(event.getPlayer().getName())){
            atm_select_players.remove(event.getPlayer().getName());
        }
    }

    @EventHandler
    public void PlayerLost(PlayerConnectionCloseEvent event){
        if (atm_select_players.contains(event.getPlayerName())){
            atm_select_players.remove(event.getPlayerName());
        }
    }
}
