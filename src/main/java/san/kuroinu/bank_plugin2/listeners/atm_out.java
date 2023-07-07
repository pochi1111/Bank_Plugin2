package san.kuroinu.bank_plugin2.listeners;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import static san.kuroinu.bank_plugin2.Bank_Plugin2.*;

public class atm_out implements Listener {
    @EventHandler
    public void InventoryClose(InventoryCloseEvent event){
        if (atm_out_players.contains(event.getPlayer().getName())){
            atm_out_players.remove(event.getPlayer().getName());
        }
    }

    @EventHandler
    public void PlayerLost(PlayerConnectionCloseEvent event){
        if (atm_out_players.contains(event.getPlayerName())){
            atm_out_players.remove(event.getPlayerName());
        }
    }

    @EventHandler
    public void InventoryClick(InventoryClickEvent event){
        if (atm_out_players.contains(event.getWhoClicked().getName())){
            Player e = (Player) event.getWhoClicked();
            ItemStack clicked_item = event.getCurrentItem();
            event.setCancelled(true);
            //money_keyのDataContainerを持っているなら
            if (clicked_item.getItemMeta().getPersistentDataContainer().has(money_key, PersistentDataType.INTEGER)){
                //その額だけお金を持っているなら
                int amount = clicked_item.getItemMeta().getPersistentDataContainer().get(money_key, PersistentDataType.INTEGER);
                if (amount < econ.getBalance(e)){
                    //インベントリに空きがあるか
                    if (e.getInventory().firstEmpty() != -1) {
                        econ.withdrawPlayer(e, amount);
                        e.getInventory().addItem(clicked_item);
                    }else{
                        e.sendMessage(prefix + ChatColor.RED + "インベントリがいっぱいです");
                    }
                }else{
                    e.sendMessage(prefix + ChatColor.RED + "お金が足りません");
                }
            }
        }
    }
}
