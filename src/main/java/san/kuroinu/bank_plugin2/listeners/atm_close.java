package san.kuroinu.bank_plugin2.listeners;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import static san.kuroinu.bank_plugin2.Bank_Plugin2.*;

public class atm_close implements Listener{
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if (atm_open_players.contains(event.getPlayer().getName())){
            //お金を入れる処理
            atm_open_players.remove(event.getPlayer().getName());
            Player e = (Player) event.getPlayer();
            Inventory close_inv = event.getInventory();
            int money = 0;
            for (int i = 0; i < 27; i++){
                ItemStack item = close_inv.getItem(i);
                if (item != null){
                    //money_keyのPersistentDataContainerがあるならお金を入れる
                    if (item.getItemMeta().getPersistentDataContainer().has(money_key, PersistentDataType.INTEGER)){
                        money += item.getItemMeta().getPersistentDataContainer().get(money_key, PersistentDataType.INTEGER)*item.getAmount();
                        close_inv.setItem(i, null);
                    }
                }
            }
            econ.depositPlayer(e, money);
            e.sendMessage(prefix + ChatColor.YELLOW + "お財布に" + money + "円入れました");
        }
    }

    @EventHandler
    public void PlayerLost(PlayerConnectionCloseEvent event){
        if (atm_open_players.contains(event.getPlayerName())){
            atm_open_players.remove(event.getPlayerName());
        }
    }
}
