package san.kuroinu.bank_plugin2;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static san.kuroinu.bank_plugin2.Bank_Plugin2.*;

public class Bank_Controll {
    public OfflinePlayer e;
    private static int balance;
    public int bank_balance(){
        Thread t;
        t = new Thread(()->{
            try {
                Connection con = ds.getConnection();
                plugin.getServer().getConsoleSender().sendMessage(prefix+ChatColor.YELLOW+e.getName()+"の銀行残高を取得します");
                PreparedStatement ps = con.prepareStatement("SELECT * FROM db_money WHERE uuid = '"+e.getUniqueId()+"'");
                ResultSet rs = ps.executeQuery();
                if (rs.next()){
                    balance = rs.getInt("money");
                    //logを出力
                    plugin.getServer().getConsoleSender().sendMessage(prefix+ChatColor.YELLOW+e.getName()+"の銀行残高は"+balance+"円です");
                }else{
                    make_bank();
                    balance = 0;
                }
                rs.close();
                ps.close();
                con.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return balance;
    }

    void make_bank(){
        new Thread(()->{
            try {
                Connection con = ds.getConnection();
                con.prepareStatement("insert into db_money (uuid, money) values ('"+e.getUniqueId()+"', 0)").executeUpdate();
                con.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public boolean deposit_bank(int amount){
        new Thread(()->{
            try {
                int now_money = bank_balance();
                now_money += amount;
                Connection con = ds.getConnection();
                con.prepareStatement("update db_money set money = "+now_money+" where uuid = '"+e.getUniqueId()+"'").executeUpdate();
                con.close();
                if (e.isOnline()) e.getPlayer().sendMessage(prefix+ ChatColor.YELLOW +amount+"円が銀行に入金されました!");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
        return true;
    }

    public boolean withdraw_bank(int amount){
        if (bank_balance() < amount){
            return false;
        }else{
            new Thread(()->{
                try {
                    int now_money = bank_balance();
                    now_money -= amount;
                    Connection con = ds.getConnection();
                    con.prepareStatement("update db_money set money = "+now_money+" where uuid = '"+e.getUniqueId()+"'").executeUpdate();
                    con.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
            return true;
        }
    }
}
