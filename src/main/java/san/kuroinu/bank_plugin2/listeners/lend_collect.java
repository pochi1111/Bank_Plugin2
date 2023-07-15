package san.kuroinu.bank_plugin2.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import san.kuroinu.bank_plugin2.Bank_Controll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static san.kuroinu.bank_plugin2.Bank_Plugin2.*;

public class lend_collect implements Listener{
    //右クリックしたとき
    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if (!event.getAction().isRightClick() || event.getPlayer().getInventory().getItemInMainHand() == null){
            return;
        }
        if (!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.PAPER)){
            return;
        }
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        Player e = event.getPlayer();
        //lend_keyを持っているか
        if (!item.getItemMeta().getPersistentDataContainer().has(lend_key)){
            return;
        }
        //lend_keyの値を取得
        int lend_key_value = item.getItemMeta().getPersistentDataContainer().get(lend_key, PersistentDataType.INTEGER);
        //それに該当するものをDBから探す
        new Thread(() -> {
            //DBから取得
            try {
                Connection con = ds.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM lend WHERE lend_key = ?");
                ps.setInt(1, lend_key_value);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    //期限の日にちが来ているか
                    if (rs.getDate("debt_term").getTime() > System.currentTimeMillis()){
                        e.sendMessage(prefix+ ChatColor.RED+"この手形はまだ有効ではありません");
                        return;
                    }
                    //相手がお金を持っているか
                    OfflinePlayer p = plugin.getServer().getOfflinePlayer(rs.getString("debtor_uuid"));
                    if (p.isOnline()){
                        int p_saihu = (int) econ.getBalance(p.getPlayer());
                        if (p_saihu >= rs.getInt("debt_amount")){
                            //お金を渡す
                            econ.withdrawPlayer(p.getPlayer(), rs.getInt("debt_amount"));
                            econ.depositPlayer(e, rs.getInt("debt_amount"));
                            //手形を削除
                            PreparedStatement ps2 = con.prepareStatement("DELETE FROM lend WHERE lend_key = ?");
                            ps2.setInt(1, lend_key_value);
                            ps2.executeUpdate();
                            ps2.close();
                            //eにメッセージを送信
                            e.sendMessage(prefix+ ChatColor.GREEN+"お金を回収しました");
                            //pにメッセージを送信
                            p.getPlayer().sendMessage(prefix+ ChatColor.GREEN+"お金を回収されました");
                            //eのインベントリからitemを削除
                            e.getInventory().remove(item);
                            return;
                        }
                    }
                    //銀行に入っているか
                    Bank_Controll bc = new Bank_Controll();
                    bc.e = p;
                    int p_saihu = bc.bank_balance();
                    if (p_saihu >= rs.getInt("debt_amount")){
                        //お金を渡す
                        bc.withdraw_bank(rs.getInt("debt_amount"));
                        econ.depositPlayer(e, rs.getInt("debt_amount"));
                        //手形を削除
                        PreparedStatement ps2 = con.prepareStatement("DELETE FROM lend WHERE lend_key = ?");
                        ps2.setInt(1, lend_key_value);
                        ps2.executeUpdate();
                        ps2.close();
                        //eにメッセージを送信
                        e.sendMessage(prefix+ ChatColor.GREEN+"お金を回収しました");
                        //pにメッセージを送信
                        if (p.isOnline()) p.getPlayer().sendMessage(prefix+ ChatColor.GREEN+"お金を回収されました");
                        //eのインベントリからitemを削除
                        e.getInventory().remove(item);
                    }
                }else{
                    e.sendMessage(prefix+ ChatColor.RED+"この手形は無効です");
                    //eのインベントリからitemを削除
                    e.getInventory().remove(item);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

        }).start();
    }
}
